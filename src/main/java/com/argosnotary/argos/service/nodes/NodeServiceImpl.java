/*
 * Argos Notary - A new way to secure the Software Supply Chain
 *
 * Copyright (C) 2019 - 2020 Rabobank Nederland
 * Copyright (C) 2019 - 2023 Gerard Borst <gerard.borst@argosnotary.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.argosnotary.argos.service.nodes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.argosnotary.argos.domain.nodes.Node;
import com.argosnotary.argos.domain.nodes.Organization;
import com.argosnotary.argos.domain.roles.Permission;
import com.argosnotary.argos.service.mongodb.nodes.NodeRepository;
import com.argosnotary.argos.service.roles.RoleAssignmentService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NodeServiceImpl implements NodeService {
	
	private final NodeRepository nodeRepository;
	private final NodeDeleteService nodeDeleteService;
    private final RoleAssignmentService roleAssignmentService;

	@Override
	public Node create(Node node) {
		return nodeRepository.insert(createNode(node));
	}
	
	@Override
	public Node update(Node node) {
		return nodeRepository.save(createNode(node));
	}
	
	private Node createNode(Node node) {
		if (node.getId() == null) {
			node.setId(UUID.randomUUID());
		}
		List<UUID> path = new ArrayList<>();
		path.add(node.getId());
		if (!(node instanceof Organization)) {
			nodeRepository.findById(node.getParentId()).ifPresent(p -> path.addAll(p.getPathToRoot()));
		}
		node.setPathToRoot(path);
		return node;
	}
	
	@Override
	public void delete(UUID resourceId) {
		Optional<Node> optNode = this.getSubTree(resourceId);
		if (!optNode.isEmpty()) {
			nodeDeleteService.deleteNode(optNode.get());
		}
	}

	@Override
	public Optional<Node> getSubTree(UUID nodeId) {
		List<Node> nodes = nodeRepository.findByPathToRoot(nodeId);
		if (nodes.isEmpty()) {
			return Optional.empty();
		}
		return Optional.of(createSubTree(nodeId, nodes));
	}
	
	private Node createSubTree(UUID nodeId, List<Node> nodes) {
		HashMap<UUID,Node> nodeMap = new HashMap<>();
		nodes.forEach(n -> nodeMap.put(n.getId(), n));
		nodes.forEach(n -> {
			if (n.getParentId() != null && nodeMap.containsKey(n.getParentId())) {
				n.setParent(nodeMap.get(n.getParentId()));
				nodeMap.get(n.getParentId()).getChildren().add(n);
			}
			});
		
		return nodeMap.get(nodeId);
	}

	@Override
	public Optional<Node> findById(UUID nodeId) {
		return nodeRepository.findById(nodeId);
	}

	@Override
	public Set<Node> find(String clazz, Optional<Node> nodeOpt) {
		Set<UUID> ids = new HashSet<>();
		if (nodeOpt.isPresent() && roleAssignmentService.findAllPermissionDownTree(nodeOpt.get()).stream()
				.anyMatch(Permission.READ::equals)) {
			ids.add(nodeOpt.get().getId());
		} else {
			// read authorization is needed
			// down tree -> implicit
			// up tree -> explicit
			
			// get authorized nodeIds
			ids = roleAssignmentService.findByIdentity().stream()
					.filter(ras -> ras.getRole().getPermissions().contains(Permission.READ))
					.map(ra -> ra.getResourceId()).collect(Collectors.toSet());
		}
		Set<Node> nodes = nodeRepository.findWithClassAndResourceIdsUpTree(clazz, ids).stream().collect(Collectors.toSet());
		
		Set<UUID> pathUuids = nodeRepository.findWithIds(ids).stream()
				.map(Node::getPathToRoot).flatMap(List::stream).collect(Collectors.toSet());
		
		nodes.addAll(nodeRepository.findWithClassAndResourceIds(clazz, pathUuids));
		return nodes;
	}

	@Override
	public boolean exists(Class clazz, UUID resourceId) {
		return nodeRepository.existsByClassAndId(clazz.getCanonicalName(), resourceId);
	}
	
	@Override
	public boolean exists(Class clazz, String name) {
		return nodeRepository.existsByClassAndName(clazz.getCanonicalName(), name);
	}

	@Override
	public Optional<String> getQualifiedName(UUID resourceId) {
		Optional<Node> nodeOpt = findRootNodeInPath(resourceId);
		if (nodeOpt.isEmpty()) {
			return Optional.empty();
		}
		List<String> labels = getQualifiedName(nodeOpt.get());
		return Optional.of(String.join(".", labels));
	}
	
	private List<String> getQualifiedName(Node node) {
		List<String> labels = new ArrayList<>();
		if (node instanceof Organization org) {
			labels.addAll((org).getDomain().reverseLabels());
		} else {
			labels.add(node.getName());
		}
		if (node.getChildren().isEmpty()) {
			return labels;
		}
		List<String> tmp = getQualifiedName(node.getChildren().iterator().next());
		labels.addAll(tmp);
		return labels;
	}

	@Override
	public Optional<Node> findRootNodeInPath(UUID resourceId) {
		Optional<Node> nodeOpt = nodeRepository.findById(resourceId);
		if (nodeOpt.isEmpty()) {
			return Optional.empty();
		}
		List<UUID> path = nodeOpt.get().getPathToRoot();
		UUID rootId = path.get(path.size()-1);
		List<Node> nodes = nodeRepository.findAllById(path);
		return Optional.of(createSubTree(rootId, nodes));

	}

	@Override
	public boolean existsByParentIdAndName(UUID parentId, String name) {
		return nodeRepository.existsByParentIdAndName(parentId, name);
	}
}
