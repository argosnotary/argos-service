package com.argosnotary.argos.service.link;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.argosnotary.argos.domain.link.LinkMetaBlock;
import com.argosnotary.argos.service.mongodb.link.LinkMetaBlockRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LinkMetaBlockServiceImpl implements LinkMetaBlockService {
	
	private final LinkMetaBlockRepository linkMetaBlockRepository; 

	@Override
	public LinkMetaBlock save(LinkMetaBlock linkMetaBlock) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deleteBySupplyChainId(UUID supplyChainId) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<LinkMetaBlock> find(UUID supplyChainId, String optionalHash) {
		return Optional.ofNullable(optionalHash)
				.map(hash -> linkMetaBlockRepository.findBySupplyChainIdAndHash(supplyChainId, hash))
                .orElseGet(() -> linkMetaBlockRepository.findBySupplyChainId(supplyChainId));
	}

}
