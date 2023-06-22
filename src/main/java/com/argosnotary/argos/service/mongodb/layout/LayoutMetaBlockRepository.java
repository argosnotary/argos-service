package com.argosnotary.argos.service.mongodb.layout;

import java.util.List;
import java.util.UUID;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.argosnotary.argos.domain.layout.LayoutMetaBlock;

public interface LayoutMetaBlockRepository extends MongoRepository<LayoutMetaBlock, UUID> {
	
	public void deleteBySupplyChainId(UUID supplyChainId);
	
	public List<LayoutMetaBlock> findBySupplyChainId(UUID supplyChainId);
}
