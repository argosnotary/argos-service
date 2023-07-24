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
package com.argosnotary.argos.service.rest.layout;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import com.argosnotary.argos.service.openapi.rest.api.LayoutApi;
import com.argosnotary.argos.service.openapi.rest.model.RestApprovalConfiguration;
import com.argosnotary.argos.service.openapi.rest.model.RestLayout;
import com.argosnotary.argos.service.openapi.rest.model.RestLayoutMetaBlock;
import com.argosnotary.argos.service.openapi.rest.model.RestReleaseConfiguration;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

public interface LayoutMetaBlockRestService extends LayoutApi {
	@Override
	public ResponseEntity<List<RestApprovalConfiguration>> createApprovalConfigurations(
			@Parameter(name = "supplyChainId", description = "supply chain id", required = true, in = ParameterIn.PATH) @PathVariable("supplyChainId") UUID supplyChainId,
			@Parameter(name = "RestApprovalConfiguration", description = "", required = true) @Valid @Size(max = 20) @RequestBody List<RestApprovalConfiguration> restApprovalConfiguration);

	@Override
	public ResponseEntity<RestLayoutMetaBlock> createOrUpdateLayout(
			@Parameter(name = "supplyChainId", description = "supply chain id", required = true, in = ParameterIn.PATH) @PathVariable("supplyChainId") UUID supplyChainId,
			@Parameter(name = "RestLayoutMetaBlock", description = "", required = true) @Valid @RequestBody RestLayoutMetaBlock restLayoutMetaBlock);

	@Override
	public ResponseEntity<RestReleaseConfiguration> createReleaseConfiguration(
			@Parameter(name = "supplyChainId", description = "supply chain id", required = true, in = ParameterIn.PATH) @PathVariable("supplyChainId") UUID supplyChainId,
			@Parameter(name = "RestReleaseConfiguration", description = "", required = true) @Valid @RequestBody RestReleaseConfiguration restReleaseConfiguration);

	@Override
	public ResponseEntity<List<RestApprovalConfiguration>> getApprovalConfigurations(
			@Parameter(name = "supplyChainId", description = "supply chain id", required = true, in = ParameterIn.PATH) @PathVariable("supplyChainId") UUID supplyChainId);

	@Override
	public ResponseEntity<List<RestApprovalConfiguration>> getApprovalsForAccount(
			@Parameter(name = "supplyChainId", description = "supply chain id", required = true, in = ParameterIn.PATH) @PathVariable("supplyChainId") UUID supplyChainId);

	@Override
	public ResponseEntity<RestLayoutMetaBlock> getLayout(
			@Parameter(name = "supplyChainId", description = "supply chain id", required = true, in = ParameterIn.PATH) @PathVariable("supplyChainId") UUID supplyChainId);

	@Override
	public ResponseEntity<RestReleaseConfiguration> getReleaseConfiguration(
			@Parameter(name = "supplyChainId", description = "supply chain id", required = true, in = ParameterIn.PATH) @PathVariable("supplyChainId") UUID supplyChainId);

	@Override
	public ResponseEntity<Void> validateLayout(
			@Parameter(name = "supplyChainId", description = "supply chain id", required = true, in = ParameterIn.PATH) @PathVariable("supplyChainId") UUID supplyChainId,
			@Parameter(name = "RestLayout", description = "", required = true) @Valid @RequestBody RestLayout restLayout);

}
