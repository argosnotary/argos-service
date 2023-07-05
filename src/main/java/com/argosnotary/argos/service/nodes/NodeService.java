package com.argosnotary.argos.service.nodes;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.argosnotary.argos.domain.nodes.Node;
import com.argosnotary.argos.domain.roles.Permission;

public interface NodeService {
	
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
	public Set<Node> find(String clazz, Optional<Node> node);
	
	public boolean exists(Class clazz, UUID resourceId);
	
	public boolean exists(Class clazz, String name);
	
	public boolean existsByParentIdAndName(UUID parentId, String name);
	
	Optional<String> getQualifiedName(UUID resourceId);
	
	Optional<Node> findRootNodeInPath(UUID resourceId);

}
