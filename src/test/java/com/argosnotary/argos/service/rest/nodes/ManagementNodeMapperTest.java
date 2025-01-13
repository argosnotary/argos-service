/*
 * Argos Notary - A new way to secure the Software Supply Chain
 *
 * Copyright (C) 2019 - 2020 Rabobank Nederland
 * Copyright (C) 2019 - 2025 Gerard Borst <gerard.borst@argosnotary.com>
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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import com.argosnotary.argos.domain.nodes.ManagementNode;
import com.argosnotary.argos.domain.nodes.Organization;
import com.argosnotary.argos.service.openapi.rest.model.RestManagementNode;

class ManagementNodeMapperTest {
	
	private ManagementNodeMapper managementNodeMapper;
	
    private Organization org2;
    private ManagementNode node21, node211;

	@BeforeEach
	void setUp() throws Exception {
		managementNodeMapper = Mappers.getMapper(ManagementNodeMapper.class);
		
		org2 = new Organization(UUID.randomUUID(), "org2", null);
        
        node21 = new ManagementNode(UUID.randomUUID(), "node21", List.of(), org2.getId());
        node211 = new ManagementNode(UUID.randomUUID(), "node211", List.of(), node21.getId());
        
        node21.setPathToRoot(List.of(node21.getId(), org2.getId()));
        node211.setPathToRoot(List.of(node211.getId(), node21.getId(), org2.getId()));
	}

	@Test
	void testMapper() {
		RestManagementNode restNode = managementNodeMapper.convertToRestManagementNode(node21);
		ManagementNode node = managementNodeMapper.convertFromRestManagementNode(restNode);
		assertEquals(node21, node);
	}

}
