package com.argosnotary.argos.service.nodes;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.argosnotary.argos.domain.nodes.ManagementNode;
import com.argosnotary.argos.domain.nodes.Node;

public interface ManagementNodeService {
	
	/**
	 * Find all management nodes where the account is authorized for
	 * @return
	 */
	Set<ManagementNode> find(Node node);
	
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
