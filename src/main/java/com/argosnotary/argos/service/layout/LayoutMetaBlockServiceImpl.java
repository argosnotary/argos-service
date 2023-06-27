package com.argosnotary.argos.service.layout;

import static java.util.Collections.emptyList;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.argosnotary.argos.domain.ArgosError;
import com.argosnotary.argos.domain.account.Account;
import com.argosnotary.argos.domain.crypto.KeyPair;
import com.argosnotary.argos.domain.layout.ApprovalConfiguration;
import com.argosnotary.argos.domain.layout.Layout;
import com.argosnotary.argos.domain.layout.LayoutMetaBlock;
import com.argosnotary.argos.domain.layout.ReleaseConfiguration;
import com.argosnotary.argos.service.account.AccountSecurityContext;
import com.argosnotary.argos.service.mongodb.layout.ApprovalConfigurationRepository;
import com.argosnotary.argos.service.mongodb.layout.LayoutMetaBlockRepository;
import com.argosnotary.argos.service.mongodb.layout.ReleaseConfigurationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LayoutMetaBlockServiceImpl implements LayoutMetaBlockService {
	
	private final LayoutMetaBlockRepository layoutMetaBlockRepository;
	
	private final ApprovalConfigurationRepository approvalConfigurationRepository;
	
	private final ReleaseConfigurationRepository releaseConfigurationRepository;    

    private final AccountSecurityContext accountSecurityContext;

	@Override
	public void deleteBySupplyChainId(UUID supplyChainId) {
		layoutMetaBlockRepository.deleteBySupplyChainId(supplyChainId);
	}

	@Override
	public LayoutMetaBlock save(LayoutMetaBlock layout) {
		return layoutMetaBlockRepository.save(layout);
	}

	@Override
	public Optional<LayoutMetaBlock> getLayout(UUID supplyChainId) {
		return layoutMetaBlockRepository.findById(supplyChainId);
	}

	@Override
	public List<ApprovalConfiguration> createApprovalConfigurations(List<ApprovalConfiguration> approvalConfigurations) {
		approvalConfigurations.forEach(a -> a.setId(UUID.randomUUID()));
		return approvalConfigurationRepository.insert(approvalConfigurations);
	}

	@Override
	public List<ApprovalConfiguration> getApprovalConfigurations(UUID supplyChainId) {
		return approvalConfigurationRepository.findBySupplyChainId(supplyChainId);
	}

	@Override
	public List<ApprovalConfiguration> getApprovalsForAccount(UUID supplyChainId) {

        Account account = accountSecurityContext.getAuthenticatedAccount().orElseThrow(() -> new ArgosError("not logged in"));

        Optional<KeyPair> optionalKeyPair = Optional.ofNullable(account.getActiveKeyPair());
        Optional<LayoutMetaBlock> optionalLayoutMetaBlock = layoutMetaBlockRepository.findBySupplyChainId(supplyChainId).stream().findFirst();

        if (optionalKeyPair.isPresent() && optionalLayoutMetaBlock.isPresent()) {
            String activeAccountKeyId = optionalKeyPair.get().getKeyId();
            Layout layout = optionalLayoutMetaBlock.get().getLayout();
            List<ApprovalConfiguration> ff = approvalConfigurationRepository.findBySupplyChainId(supplyChainId);
            List<ApprovalConfiguration> ff2 = approvalConfigurationRepository.findBySupplyChainId(supplyChainId).stream()
            		.filter(approvalConf -> canApprove(approvalConf, activeAccountKeyId, layout))
            		.collect(Collectors.toList());
            return approvalConfigurationRepository.findBySupplyChainId(supplyChainId).stream()
            		.filter(approvalConf -> canApprove(approvalConf, activeAccountKeyId, layout))
            		.collect(Collectors.toList());
        } else {
            return emptyList();
        }
	}
    
    private boolean canApprove(ApprovalConfiguration approvalConf, String activeAccountKeyId, Layout layout) {
        Optional<Boolean> canApprove = layout.getSteps().stream()
                .filter(step -> step.getName().equals(approvalConf.getStepName()))
                .map(step -> step.getAuthorizedKeyIds().contains(activeAccountKeyId)).findFirst();
        return canApprove.isPresent() && canApprove.get();
    }

	@Override
	public boolean stepNameExistInLayout(Layout layout, String stepName) {
		return layout.getSteps().stream().map(s -> s.getName()).filter(n -> n.equals(stepName)).findFirst().isPresent();
	}

	@Override
	public ReleaseConfiguration createReleaseConfiguration(ReleaseConfiguration releaseConfiguration) {
		return releaseConfigurationRepository.insert(releaseConfiguration);
	}

	@Override
	public Optional<ReleaseConfiguration> getReleaseConfiguration(UUID supplyChainId) {
		return releaseConfigurationRepository.findBySupplyChainId(supplyChainId).stream().findFirst();
	}

}
