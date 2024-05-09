/*
 * Argos Notary - A new way to secure the Software Supply Chain
 *
 * Copyright (C) 2019 - 2020 Rabobank Nederland
 * Copyright (C) 2019 - 2024 Gerard Borst <gerard.borst@argosnotary.com>
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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.argosnotary.argos.domain.ArgosError;
import com.argosnotary.argos.domain.nodes.Node;
import com.argosnotary.argos.domain.nodes.Organization;
import com.argosnotary.argos.domain.nodes.TreeNode;
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
		// for update node should exist
		if (node.getId() != null && nodeRepository.existsById(node.getId())) {
			return nodeRepository.save(createNode(node));
		} else {
			throw new ArgosError(String.format("%s doesn't exist", node.getClass().getSimpleName()));
		}
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
		nodeDeleteService.deleteNode(TreeNode.createUpTree(nodeRepository.findByPathToRoot(resourceId).stream().collect(Collectors.toSet())));
	}

	@Override
	public Optional<Node> findById(UUID nodeId) {
		return nodeRepository.findById(nodeId);
	}

	/**
	 * Find nodes of Class clazz
	 * If nodeOpt is present find nodes of Class clazz in path to root and up tree
	 */
	@Override
	public Set<Node> find(String clazz, Optional<Node> nodeOpt) {
		Set<Node> nodes = new HashSet<>();
		if (nodeOpt.isPresent() && roleAssignmentService.findAllPermissionDownTree(nodeOpt.get()).stream()
				.anyMatch(Permission.READ::equals)) {
			// tree constraint by nodeOpt
			nodes.add(nodeOpt.get());
		} else {
			// nodeOpt empty -> no constraint
			// find all nodes for authorized nodes
			// read authorization is needed
			// down tree -> implicit
			// up tree -> explicit
			
			// get authorized nodeIds
			nodes.addAll(nodeRepository.findWithIds(roleAssignmentService.findByIdentity().stream()
					.filter(ras -> ras.getRole().getPermissions().contains(Permission.READ))
					.map(ra -> ra.getResourceId()).collect(Collectors.toSet())));
			
		}
		Set<Node> nodesUpTree = nodeRepository.findWithClassAndResourceIdsUpTree(clazz, nodes.stream().map(Node::getId).collect(Collectors.toSet())).stream().collect(Collectors.toSet());
		Set<UUID> pathUuids = nodeRepository.findWithIds(nodes.stream().map(Node::getPathToRoot).flatMap(List::stream).collect(Collectors.toSet())).stream().map(Node::getId).collect(Collectors.toSet());
		
		nodesUpTree.addAll(nodeRepository.findWithClassAndResourceIds(clazz, pathUuids));
		return nodesUpTree;
	}

	@Override
	public boolean exists(Class<? extends Node> clazz, UUID resourceId) {
		return nodeRepository.existsByClassAndId(clazz.getCanonicalName(), resourceId);
	}
	
	@Override
	public boolean exists(Class<? extends Node> clazz, String name) {
		return nodeRepository.existsByClassAndName(clazz.getCanonicalName(), name);
	}

	@Override
	public Optional<String> getQualifiedName(UUID resourceId) {
		Optional<Node> nodeOpt = nodeRepository.findById(resourceId);
		if (nodeOpt.isEmpty()) {
			return Optional.empty();
		}
		List<Node> pathNodes = nodeRepository.findAllById(nodeOpt.get().getPathToRoot());
		List<String> labels = getLabelsToRoot(TreeNode.getPathToRoot(nodeOpt.get(), new HashSet<>(pathNodes)));
		Collections.reverse(labels);
		return Optional.of(String.join(".", labels));
	}
	
	private List<String> getLabelsToRoot(TreeNode node) {
		List<String> labels = new ArrayList<>();
		if (node.getNode() instanceof Organization org) {
			labels.addAll(((Organization)node.getNode()).getDomain().getLabels());
		} else {
			labels.add(node.getNode().getName());
			labels.addAll(getLabelsToRoot(node.getParent().orElseThrow()));
		}
		return labels;
	}

	@Override
	public Organization findOrganizationInPath(UUID resourceId) {
		Optional<Node> nodeOpt = nodeRepository.findById(resourceId);
		List<UUID> path = nodeOpt.orElseThrow().getPathToRoot();
		UUID rootId = path.get(path.size()-1);
		Optional<Node> org = nodeRepository.findById(rootId);
		return (Organization) org.orElseThrow();
	}

	@Override
	public boolean existsByParentIdAndName(UUID parentId, String name) {
		return nodeRepository.existsByParentIdAndName(parentId, name);
	}
}
