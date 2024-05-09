/*
 * Argos Notary - A new way to secure the Software Supply Chain
 *
 * Copyright (C) 2019 - 2020 Rabobank Nederland
 * Copyright (C) 2019 - 2024 Gerard Borst <gerard.borst@argosnotary.com>
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
package com.argosnotary.argos.service.rest.nodes;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.argosnotary.argos.service.openapi.rest.api.OrganizationApi;
import com.argosnotary.argos.service.openapi.rest.model.RestOrganization;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import jakarta.validation.Valid;

public interface OrganizationRestService extends OrganizationApi {

	@Override
	ResponseEntity<RestOrganization> createOrganization(
	        @Parameter(name = "RestOrganization", description = "", required = true) @Valid @RequestBody RestOrganization restOrganization);

	@Override
	ResponseEntity<Void> deleteOrganizationById(
	        @Parameter(name = "organizationId", description = "organization id", required = true, in = ParameterIn.PATH) @PathVariable("organizationId") UUID organizationId);

	@Override
	ResponseEntity<RestOrganization> getOrganization(
	        @Parameter(name = "organizationId", description = "this will be the organizationId id", required = true, in = ParameterIn.PATH) @PathVariable("organizationId") UUID organizationId);

	@Override
	ResponseEntity<List<RestOrganization>> getOrganizations(
	        @Parameter(name = "nodeId", description = "this is be id of the node in tree", in = ParameterIn.QUERY) @Valid @RequestParam(value = "nodeId", required = false) UUID nodeId);

}
