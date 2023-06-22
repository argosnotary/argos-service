package com.argosnotary.argos.service.layout;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.argosnotary.argos.domain.layout.LayoutMetaBlock;

@Service
public class LayoutMetaBlockServiceImpl implements LayoutMetaBlockService {

	@Override
	public void deleteBySupplyChainId(UUID supplyChainId) {
		// TODO Auto-generated method stub

	}

	@Override
	public LayoutMetaBlock save(LayoutMetaBlock layout) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<LayoutMetaBlock> findBySupplyChainId(UUID supplyChainId) {
		// TODO Auto-generated method stub
		return Optional.empty();
	}

}
