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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import com.argosnotary.argos.domain.nodes.Domain;
import com.argosnotary.argos.domain.nodes.ManagementNode;
import com.argosnotary.argos.domain.nodes.Organization;
import com.argosnotary.argos.service.openapi.rest.model.RestOrganization;

class OrganizationMapperTest {
	
	private OrganizationMapper organizationMapper;
	
    private Organization org2;
    private ManagementNode node21, node22;

	@BeforeEach
	void setUp() throws Exception {
		organizationMapper = Mappers.getMapper(OrganizationMapper.class);
		
		org2 = new Organization(UUID.randomUUID(), "org2", null);
        
        org2.setDomain(Domain.builder().build());
        
        node21 = new ManagementNode(UUID.randomUUID(), "node21", org2);
        node22 = new ManagementNode(UUID.randomUUID(), "node22", org2);
	}

	@Test
	void testMapper() {
		RestOrganization restOrg = organizationMapper.convertToRestOrganization(org2);
		Organization org = organizationMapper.convertFromRestOrganization(restOrg);
		assertThat(org.getChildren(), is(Set.of()));
		assertThat(org.getDomain(), is(Domain.builder().build()));
		assertThat(org.getParent(), is(Optional.empty()));
	}

}
