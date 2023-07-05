package com.argosnotary.argos.service.link;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.argosnotary.argos.domain.link.LinkMetaBlock;

public interface LinkMetaBlockService {
	public LinkMetaBlock create(LinkMetaBlock linkMetaBlock);
	
	public void deleteBySupplyChainId(UUID supplyChainId);
    
	public List<LinkMetaBlock> find(UUID supplyChainId, Optional<String> hash);

}
