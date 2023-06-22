package com.argosnotary.argos.service.layout;

import java.util.List;
import java.util.UUID;

import com.argosnotary.argos.domain.layout.ApprovalConfiguration;

public interface ApprovalConfigurationService {
	List<ApprovalConfiguration> save(UUID supplyChainId, List<ApprovalConfiguration> approvalConfigurations);

    List<ApprovalConfiguration> findBySupplyChainId(UUID supplyChainId);

    void deleteBySupplyChainId(UUID supplyChainId);

}
