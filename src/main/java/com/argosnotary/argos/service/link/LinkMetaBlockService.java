package com.argosnotary.argos.service.link;

import java.util.List;
import java.util.UUID;

import com.argosnotary.argos.domain.link.LinkMetaBlock;

public interface LinkMetaBlockService {
	public LinkMetaBlock save(LinkMetaBlock linkMetaBlock);
	
	public void deleteBySupplyChainId(UUID supplyChainId);
	
	public List<LinkMetaBlock> findBySupplyChainId(UUID supplyChainId);
    
	public List<LinkMetaBlock> findBySupplyChainAndSha(UUID supplyChainId, String hash);

}
