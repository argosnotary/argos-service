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
