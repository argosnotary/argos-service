package com.argosnotary.argos.service.link;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.argosnotary.argos.domain.link.LinkMetaBlock;
import com.argosnotary.argos.service.mongodb.link.LinkMetaBlockRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LinkMetaBlockServiceImpl implements LinkMetaBlockService {
	
	private final LinkMetaBlockRepository linkMetaBlockRepository; 

	@Override
	public LinkMetaBlock create(LinkMetaBlock linkMetaBlock) {
		return linkMetaBlockRepository.insert(linkMetaBlock);
	}

	@Override
	public void deleteBySupplyChainId(UUID supplyChainId) {
		linkMetaBlockRepository.deleteBySupplyChainId(supplyChainId);
	}

	@Override
	public List<LinkMetaBlock> find(UUID supplyChainId, Optional<String> optionalHash) {
		if (optionalHash.isPresent()) {
			List<LinkMetaBlock> ff = linkMetaBlockRepository.findBySupplyChainIdAndHash(supplyChainId, optionalHash.get());
		}
		return optionalHash
				.map(hash -> linkMetaBlockRepository.findBySupplyChainIdAndHash(supplyChainId, hash))
                .orElseGet(() -> linkMetaBlockRepository.findBySupplyChainId(supplyChainId));
	}

}
