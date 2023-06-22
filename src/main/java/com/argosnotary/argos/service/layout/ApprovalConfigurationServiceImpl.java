package com.argosnotary.argos.service.layout;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.argosnotary.argos.domain.layout.ApprovalConfiguration;

@Service
public class ApprovalConfigurationServiceImpl implements ApprovalConfigurationService {

	@Override
	public List<ApprovalConfiguration> save(UUID supplyChainId, List<ApprovalConfiguration> approvalConfigurations) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ApprovalConfiguration> findBySupplyChainId(UUID supplyChainId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deleteBySupplyChainId(UUID supplyChainId) {
		// TODO Auto-generated method stub

	}

}
