package com.argosnotary.argos.service.mongodb.link;

import java.util.List;
import java.util.UUID;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.argosnotary.argos.domain.link.LinkMetaBlock;

public interface LinkMetaBlockRepository extends MongoRepository<LinkMetaBlock, UUID> {
	
	public void deleteBySupplyChainId(UUID supplyChainId);
	
	public List<LinkMetaBlock> findBySupplyChainId(UUID supplyChainId);
	
	@Query("{$and: [{supplyChainId: ?0},{$or:[{'link.materials.hash': ?1}, {'link.products.hash': ?1}]}]}")
	public List<LinkMetaBlock> findBySupplyChainIdAndHash(UUID supplyChainId, String hash);
}
