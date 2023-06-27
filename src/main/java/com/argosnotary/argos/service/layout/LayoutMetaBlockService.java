package com.argosnotary.argos.service.layout;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.argosnotary.argos.domain.layout.ApprovalConfiguration;
import com.argosnotary.argos.domain.layout.Layout;
import com.argosnotary.argos.domain.layout.LayoutMetaBlock;
import com.argosnotary.argos.domain.layout.ReleaseConfiguration;

public interface LayoutMetaBlockService {

	public void deleteBySupplyChainId(UUID supplyChainId);
	
	public LayoutMetaBlock save(LayoutMetaBlock layout);
	
	public Optional<LayoutMetaBlock> getLayout(UUID supplyChainId);
	
	public List<ApprovalConfiguration> createApprovalConfigurations(List<ApprovalConfiguration> approvalConfigurations);
	
	public List<ApprovalConfiguration> getApprovalConfigurations(UUID supplyChainId);
	
	public List<ApprovalConfiguration> getApprovalsForAccount(UUID supplyChainId);
	
	public ReleaseConfiguration createReleaseConfiguration(ReleaseConfiguration releaseConfiguration);
	
	public Optional<ReleaseConfiguration> getReleaseConfiguration(UUID supplyChainId);
	
	public boolean stepNameExistInLayout(Layout layout, String stepName);

}
