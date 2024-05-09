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
package com.argosnotary.argos.service.rest.roles;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import com.argosnotary.argos.domain.roles.Permission;
import com.argosnotary.argos.domain.roles.Role;
import com.argosnotary.argos.domain.roles.RoleAssignment;
import com.argosnotary.argos.service.openapi.rest.model.RestRoleAssignment;

class RoleAssignmentMapperTest {

	RoleAssignmentMapper mapper = Mappers.getMapper(RoleAssignmentMapper.class);

	@BeforeEach
	void setUp() throws Exception {
	}

	@Test
	void test() {
		RoleAssignment ra = RoleAssignment.builder()
				.id(UUID.randomUUID())
				.resourceId(UUID.randomUUID())
				.identityId(UUID.randomUUID())
				.role(new Role.Owner()).build();
		
		RestRoleAssignment rra = mapper.convertToRestRoleAssignment(ra);
		assertEquals(rra.getId(), ra.getId());
		assertEquals(rra.getIdentityId(), ra.getIdentityId());
		assertEquals(rra.getResourceId(), ra.getResourceId());
		
		RoleAssignment ran = mapper.convertFromRestRoleAssignment(mapper.convertToRestRoleAssignment(ra));
		assertEquals(ra, ran);
	}
	
	@Test
	void testRestPermissionListToPermissionSet() {
		Permission p1 = Permission.LINK_ADD;
		Permission p2 = Permission.READ;
		RoleAssignment ra = RoleAssignment.builder().role(new Role()).build();
		RestRoleAssignment rra = mapper.convertToRestRoleAssignment(ra);
		assertEquals(null, rra.getRole().getPermissions());
		
		RoleAssignment raNew = mapper.convertFromRestRoleAssignment(rra);
		assertEquals(null, raNew.getRole().getPermissions());
 	}

}
