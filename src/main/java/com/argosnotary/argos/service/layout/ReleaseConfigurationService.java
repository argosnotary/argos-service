package com.argosnotary.argos.service.layout;

import java.util.Optional;
import java.util.UUID;

import com.argosnotary.argos.domain.layout.ReleaseConfiguration;

public interface ReleaseConfigurationService {
	
	Optional<ReleaseConfiguration> findBySupplyChainId(UUID supplyChainId);
	
	ReleaseConfiguration save(ReleaseConfiguration releaseConfiguration);

    void deleteBySupplyChainId(UUID supplyChainId);

}
