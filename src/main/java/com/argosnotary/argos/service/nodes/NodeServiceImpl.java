package com.argosnotary.argos.service.nodes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.argosnotary.argos.domain.account.Account;
import com.argosnotary.argos.domain.account.ServiceAccount;
import com.argosnotary.argos.domain.nodes.Node;
import com.argosnotary.argos.domain.nodes.Organization;
import com.argosnotary.argos.domain.roles.Permission;
import com.argosnotary.argos.service.account.AccountSecurityContext;
import com.argosnotary.argos.service.mongodb.nodes.NodeRepository;
import com.argosnotary.argos.service.roles.RoleAssignmentService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NodeServiceImpl implements NodeService {
	
	private final NodeRepository nodeRepository;
	private final NodeDeleteService nodeDeleteService;
    private final RoleAssignmentService roleAssignmentService;
	private final AccountSecurityContext accountSecurityContext;

	@Override
	public Set<Permission> getAllPermissionDownTree(UUID resourceId) {
		Optional<Account> optAccount  = accountSecurityContext.getAuthenticatedAccount();
		if (optAccount.isEmpty()) {
			return Set.of();
		}
		if (optAccount.get() instanceof ServiceAccount) {
			if (resourceId.equals(((ServiceAccount)optAccount.get()).getProjectId())) {
				return ServiceAccount.defaultPermissions;
			}
			return Set.of();
		}
		// account is personal account
		Optional<Node> optNode = nodeRepository.findById(resourceId);
		if (optNode.isEmpty()) {
			return Set.of();
		}
		
		return roleAssignmentService.findByNodeAndIdentityId(optNode.get(), optAccount.get().getId());
	}

	@Override
	public Node create(Node node) {
		if (node.getId() == null) {
			node.setId(UUID.randomUUID());
		}
		List<UUID> path = new ArrayList<>();
		if (!(node instanceof Organization)) {
			path.addAll(nodeRepository.findById(node.getParentId()).get().getPathToRoot());
		}
		path.add(node.getId());
		node.setPathToRoot(path);
		return nodeRepository.save(node);
	}
	
	@Override
	public Node update(Node node) {
		List<UUID> path = new ArrayList<>();
		if (!(node instanceof Organization)) {
			path.addAll(nodeRepository.findById(node.getParentId()).get().getPathToRoot());
		}
		path.add(node.getId());
		node.setPathToRoot(path);
		return nodeRepository.save(node);
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
		List<Node> nodes = nodeRepository.findInPathToRoot(nodeId);
		if (nodes.isEmpty()) {
			return Optional.empty();
		}
		HashMap<UUID,Node> nodeMap = new HashMap<>();
		nodes.forEach(n -> nodeMap.put(n.getId(), n));
		nodes.forEach(n -> {
			if (n.getParentId() != null && nodeMap.containsKey(n.getParentId())) {
				n.setParent(nodeMap.get(n.getParentId()));
				nodeMap.get(n.getParentId()).getChildren().add(n);
			}
			});
		return Optional.of(nodeMap.get(nodeId));
	}

	@Override
	public Optional<Node> findById(UUID nodeId) {
		return nodeRepository.findById(nodeId);
	}

	@Override
	public List<Node> find(String clazz, Set<UUID> resourceIds) {
		Optional<Account> optAccount  = accountSecurityContext.getAuthenticatedAccount();
		if (optAccount.isEmpty()) {
			return List.of();
		}
		// read authorization is needed
		// down tree -> implicit
		// up tree -> explicit
		
		// get authorized nodeIds
		Set<UUID> ids = roleAssignmentService.findByIdentityId(optAccount.get().getId()).stream()
				.map(ra -> ra.getResourceId()).collect(Collectors.toSet());
		Set<UUID> intersectSet = new HashSet<>();
		if (resourceIds.isEmpty()) {
			intersectSet.addAll(ids);
		} else {
			intersectSet.addAll(resourceIds);
			// node ids in resourceIds which are authorized
			intersectSet.retainAll(getNodeIdsWithReadAuthorization(ids));
		}
		List<Node> nodes = nodeRepository.findWithClassAndResourceIdsUpTree(clazz, intersectSet);
		
		Set<UUID> pathUuids = nodeRepository.findWithIds(intersectSet).stream()
				.map(n -> n.getPathToRoot()).flatMap(List::stream).collect(Collectors.toSet());
		
		nodes.addAll(nodeRepository.findWithClassAndResourceIds(clazz, pathUuids));
		return nodes;
	}

	private Set<UUID> getNodeIdsWithReadAuthorization(Set<UUID> expAuthIds) {
		Set<UUID> pathIds = nodeRepository.findWithIds(expAuthIds).stream()
				.map(n -> n.getPathToRoot()).flatMap(List::stream).collect(Collectors.toSet());
		Set<UUID> upTreeIds = nodeRepository.findWithResourceIdsUpTree(expAuthIds).stream()
				.map(n -> n.getId()).collect(Collectors.toSet());
		pathIds.addAll(upTreeIds);
		
		return pathIds;
	}

	@Override
	public boolean exists(String clazz, UUID resourceId) {
		return nodeRepository.existsByClassAndId(clazz, resourceId);
	}

	@Override
	public Optional<String> getFullDomainName(UUID resourceId) {
		Optional<Node> node = nodeRepository.findById(resourceId);
		if (node.isEmpty()) {
			return Optional.empty();
		}
		Map<UUID,Node> nodeMap = new HashMap();
		nodeRepository.findWithResourceIds(node.get().getPathToRoot()).stream().forEach(n -> nodeMap.put(n.getId(), n));
		String fullDomainName = nodeMap.get(resourceId).getPathToRoot().stream().map(n -> {
			Node pathNode = nodeMap.get(n);
			if (pathNode instanceof Organization) {
				return ((Organization) pathNode).getDomain().getDomain();
			} else {
				return pathNode.getName();
			}
		}).collect(Collectors.joining("."));
		return Optional.of(fullDomainName);
	}
}
