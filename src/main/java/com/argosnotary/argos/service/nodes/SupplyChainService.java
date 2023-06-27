package com.argosnotary.argos.service.nodes;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.argosnotary.argos.domain.nodes.Node;
import com.argosnotary.argos.domain.nodes.Organization;
import com.argosnotary.argos.domain.nodes.SupplyChain;

public interface SupplyChainService {
	
	Set<SupplyChain>  find(Node node);
	
	SupplyChain create(SupplyChain supplyChain);
	
	SupplyChain update(SupplyChain supplyChain);
	
	void delete(UUID supplyChainId);
	
	Optional<SupplyChain> findById(UUID supplyChainId);
	
	boolean exists(UUID supplyChainId);
	
	Optional<String> getQualifiedName(UUID supplyChainId);
	
	Optional<Organization> getOrganization(UUID supplyChainId);

}
