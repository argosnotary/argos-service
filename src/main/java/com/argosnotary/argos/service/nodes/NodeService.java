package com.argosnotary.argos.service.nodes;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.argosnotary.argos.domain.nodes.Node;
import com.argosnotary.argos.domain.roles.Permission;

public interface NodeService {
	
	public Set<Permission> getAllPermissionDownTree(UUID resourceId);
	
	public Node create(Node node);
	
	public Node update(Node node);
	
	void delete(UUID resourceId);
	
	public Optional<Node> getSubTree(UUID nodeId);
	
	public Optional<Node> findById(UUID nodeId);
	
	/**
	 * Find nodes with Class clazz and authorized resourceIds in path to root and to leafs. 
	 * If resourceIds is empty return all authorized nodes
	 * @param clazz
	 * @param resourceIds
	 * @return
	 */
	public List<Node> find(String clazz, Set<UUID> resourceIds);
	
	public boolean exists(String clazz, UUID resourceId);
	
	Optional<String> getFullDomainName(UUID resourceId);

}
