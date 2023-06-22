package com.argosnotary.argos.service.layout;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.argosnotary.argos.domain.layout.LayoutMetaBlock;

public interface LayoutMetaBlockService {

	public void deleteBySupplyChainId(UUID supplyChainId);
	
	public LayoutMetaBlock save(LayoutMetaBlock layout);
	
	public Optional<LayoutMetaBlock> findBySupplyChainId(UUID supplyChainId);

}
