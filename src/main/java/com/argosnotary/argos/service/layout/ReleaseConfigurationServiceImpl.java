package com.argosnotary.argos.service.layout;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.argosnotary.argos.domain.layout.ReleaseConfiguration;

@Service
public class ReleaseConfigurationServiceImpl implements ReleaseConfigurationService {

	@Override
	public Optional<ReleaseConfiguration> findBySupplyChainId(UUID supplyChainId) {
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public ReleaseConfiguration save(ReleaseConfiguration releaseConfiguration) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deleteBySupplyChainId(UUID supplyChainId) {
		// TODO Auto-generated method stub

	}

}
