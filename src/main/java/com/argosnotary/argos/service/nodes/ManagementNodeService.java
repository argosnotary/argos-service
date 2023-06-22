package com.argosnotary.argos.service.nodes;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.argosnotary.argos.domain.nodes.ManagementNode;

public interface ManagementNodeService {
	
	/**
	 * Find all management nodes where the account is authorized for
	 * @return
	 */
	List<ManagementNode> find(Set<UUID> resourceIds);
	
	/**
	 * 
	 * @param managementNodeId
	 * @return
	 */
	Optional<ManagementNode> findById(UUID managementNodeId);
	
	/**
	 * Create an ManagementNode
	 * @param managementNode
	 * @return
	 */
	ManagementNode create(ManagementNode managementNode);
	
	void delete(UUID managementNodeId);
}
