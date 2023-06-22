package com.argosnotary.argos.service.link;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.argosnotary.argos.domain.link.LinkMetaBlock;

@Service
public class LinkMetaBlockServiceImpl implements LinkMetaBlockService {

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
	public List<LinkMetaBlock> findBySupplyChainId(UUID supplyChainId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<LinkMetaBlock> findBySupplyChainAndSha(UUID supplyChainId, String hash) {
		// TODO Auto-generated method stub
		return null;
	}

}
