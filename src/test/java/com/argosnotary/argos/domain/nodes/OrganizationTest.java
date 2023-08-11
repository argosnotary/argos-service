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
package com.argosnotary.argos.domain.nodes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OrganizationTest {
    private Organization org1, org2;
    private Project project1, project2, project3, project4, project5;
    private ManagementNode node1, node2, node3, node4;

	@BeforeEach
	void setUp() throws Exception {

		org1 = new Organization(UUID.randomUUID(), "org1", null);

		org2 = new Organization(UUID.randomUUID(), "org2", null);
		
	}
	
	@Test
	void testEquals() {
		Organization expected = new Organization(org2.getId(), "org2", null);
		assertEquals(expected, org2);
		
	}
	
	@Test
	void testResourceId() {
		UUID aId = UUID.randomUUID();
		Organization org1 = new Organization(aId, "org1", null);
		assertThat(org1.getId()).isEqualTo(aId);
	}
	
	@Test
	void testGetParentId() {
		assertNull(org1.getParentId());
	}
	
	@Test
	void testParentType() {
		assertFalse(org1.isValidParentType(new SupplyChain()));
		assertFalse(org1.isValidParentType(new Project()));
		assertFalse(org1.isValidParentType(new ManagementNode()));
		assertFalse(org1.isValidParentType(new Organization()));
	}

}
