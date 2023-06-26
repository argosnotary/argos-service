package com.argosnotary.argos.service.layout;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.argosnotary.argos.domain.layout.LayoutMetaBlock;
import com.argosnotary.argos.service.mongodb.layout.LayoutMetaBlockRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LayoutMetaBlockServiceImpl implements LayoutMetaBlockService {
	
	private final LayoutMetaBlockRepository layoutMetaBlockRepository; 

	@Override
	public void deleteBySupplyChainId(UUID supplyChainId) {
		layoutMetaBlockRepository.deleteBySupplyChainId(supplyChainId);
	}

	@Override
	public LayoutMetaBlock save(LayoutMetaBlock layout) {
		return layoutMetaBlockRepository.save(layout);
	}

	@Override
	public Optional<LayoutMetaBlock> findBySupplyChainId(UUID supplyChainId) {
		return layoutMetaBlockRepository.findById(supplyChainId);
	}

}
