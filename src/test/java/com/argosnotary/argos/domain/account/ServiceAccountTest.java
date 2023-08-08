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
package com.argosnotary.argos.domain.account;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.argosnotary.argos.domain.crypto.KeyPair;
import com.argosnotary.argos.domain.roles.Role;
import com.argosnotary.argos.domain.roles.RoleAssignment;

class ServiceAccountTest {

	@BeforeEach
	void setUp() throws Exception {
	}

	@Test
	void testConstructor() {
		UUID projectId = UUID.randomUUID();
		RoleAssignment releaser = RoleAssignment.builder().resourceId(projectId).role(new Role.Releaser()).build();
		RoleAssignment la = RoleAssignment.builder().resourceId(projectId).role(new Role.LinkAdder()).build();
		
		ServiceAccount sa = ServiceAccount.builder().name("sa").projectId(projectId).build();
		assertNotNull(sa.getId());
	}
	
	@Test
	void testResourceId() {
		UUID aId = UUID.randomUUID();
		ServiceAccount sa = ServiceAccount.builder().name("sa").id(aId).build();
		assertThat(sa.getId()).isEqualTo(aId);
	}
	

	
	@Test
	void testInActiveKeys() {
		UUID aId = UUID.randomUUID();
		KeyPair kp = new KeyPair("arg1", "arg2".getBytes(), "arg3".getBytes());
		ServiceAccount sa = ServiceAccount.builder().name("sa").id(aId).inactiveKeyPairs(Set.of(kp)).build();
		assertThat(sa.getInactiveKeyPairs()).isEqualTo(Set.of(kp));
		sa = ServiceAccount.builder().name("sa").id(aId).inactiveKeyPairs(Set.of()).build();
		assertThat(sa.getInactiveKeyPairs()).isEmpty();
	}

}
