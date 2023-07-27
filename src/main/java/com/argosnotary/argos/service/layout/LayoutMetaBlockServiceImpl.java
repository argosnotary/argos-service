/*
 * Argos Notary - A new way to secure the Software Supply Chain
 *
 * Copyright (C) 2019 - 2020 Rabobank Nederland
 * Copyright (C) 2019 - 2023 Gerard Borst <gerard.borst@argosnotary.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.argosnotary.argos.service.layout;

import static java.util.Collections.emptyList;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.argosnotary.argos.domain.ArgosError;
import com.argosnotary.argos.domain.account.Account;
import com.argosnotary.argos.domain.crypto.KeyPair;
import com.argosnotary.argos.domain.layout.ApprovalConfiguration;
import com.argosnotary.argos.domain.layout.Layout;
import com.argosnotary.argos.domain.layout.LayoutMetaBlock;
import com.argosnotary.argos.domain.layout.ReleaseConfiguration;
import com.argosnotary.argos.domain.layout.Step;
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
            return approvalConfigurationRepository.findBySupplyChainId(supplyChainId).stream()
            		.filter(approvalConf -> canApprove(approvalConf, activeAccountKeyId, layout))
            		.toList();
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
		return layout.getSteps().stream().map(Step::getName).anyMatch(n -> n.equals(stepName));
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
