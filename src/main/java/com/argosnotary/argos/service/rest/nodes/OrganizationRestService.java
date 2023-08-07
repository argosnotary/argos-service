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
package com.argosnotary.argos.service.rest.nodes;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;

import com.argosnotary.argos.service.openapi.rest.api.OrganizationApi;
import com.argosnotary.argos.service.openapi.rest.model.RestOrganization;

public interface OrganizationRestService extends OrganizationApi {

	@Override
	public ResponseEntity<RestOrganization> createOrganization(RestOrganization restOrganization);

	@Override
	public ResponseEntity<Void> deleteOrganizationById(UUID organizationId);

	@Override
	public ResponseEntity<RestOrganization> getOrganization(UUID organizationId);

	@Override
	public ResponseEntity<List<RestOrganization>> getOrganizations(UUID nodeId);

}
