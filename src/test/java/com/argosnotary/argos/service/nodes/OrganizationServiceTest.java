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
package com.argosnotary.argos.service.nodes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.argosnotary.argos.domain.account.PersonalAccount;
import com.argosnotary.argos.domain.nodes.Domain;
import com.argosnotary.argos.domain.nodes.ManagementNode;
import com.argosnotary.argos.domain.nodes.Organization;
import com.argosnotary.argos.domain.roles.Role;
import com.argosnotary.argos.service.account.AccountSecurityContext;
import com.argosnotary.argos.service.roles.RoleAssignmentService;

@ExtendWith(MockitoExtension.class)
class OrganizationServiceTest {
	OrganizationService organizationService;
	
	@Mock
	private RoleAssignmentService roleAssignmentService;

	@Mock
    private AccountSecurityContext accountSecurityContext;
	
	@Mock
	NodeService nodeService;
	
	@Mock
	PersonalAccount pa;
	
	private Organization org1;
	private ManagementNode node11, node111, node112;

	@BeforeEach
	void setUp() throws Exception {
		organizationService = new OrganizationServiceImpl(nodeService, roleAssignmentService, accountSecurityContext);
        org1 = new Organization(UUID.randomUUID(), "org1", Domain.builder().name("org1.com").build());
        
        node11 = new ManagementNode(UUID.randomUUID(), "node11", List.of(), org1.getId());
        node111 = new ManagementNode(UUID.randomUUID(), "node111", List.of(), node11.getId());
        node112 = new ManagementNode(UUID.randomUUID(), "node112", List.of(), node11.getId());
        
        node11.setPathToRoot(List.of(node11.getId(), org1.getId()));
        node111.setPathToRoot(List.of(node111.getId(), node11.getId(), org1.getId()));
        node112.setPathToRoot(List.of(node112.getId(), node11.getId(), org1.getId()));
	}
	
	@Test
	void testFindNotEmpty() {
		when(nodeService.find(Organization.class.getCanonicalName(), Optional.of(node11))).thenReturn(Set.of(org1));
		Set<Organization> nodes = organizationService.find(Optional.of(node11));
		assertEquals(Set.of(org1), nodes);
	}
	
	@Test
	void testFindEmpty() {
		when(nodeService.find(Organization.class.getCanonicalName(), Optional.empty())).thenReturn(Set.of(org1));
		Set<Organization> nodes = organizationService.find(Optional.empty());
		assertEquals(Set.of(org1), nodes);
	}


	
	@Test
	void testFindById() {
		when(nodeService.findById(org1.getId())).thenReturn(Optional.of(org1));
		Optional<Organization> node = organizationService.findById(org1.getId());
		assertEquals(org1, node.get());
	}
	
	@Test
	void testFindByIdEmpty() {
		when(nodeService.findById(org1.getId())).thenReturn(Optional.empty());
		Optional<Organization> node = organizationService.findById(org1.getId());
		assertTrue(node.isEmpty());
	}
	
	@Test
	void testFindByIdNotCorrectType() {
		when(nodeService.findById(org1.getId())).thenReturn(Optional.of(node11));
		Optional<Organization> node = organizationService.findById(org1.getId());
		assertTrue(node.isEmpty());
	}
	
	@Test
	void testExistsByNameTrue() {
		when(nodeService.exists(Organization.class, org1.getName())).thenReturn(true);
		assertTrue(organizationService.existsByName(org1.getName()));
	}
	
	@Test
	void testExistsByNameFalse() {
		when(nodeService.exists(Organization.class, org1.getName())).thenReturn(false);
		assertFalse(organizationService.existsByName(org1.getName()));
	}
	
	@Test
	void testExistsTrue() {
		when(nodeService.exists(Organization.class, org1.getId())).thenReturn(true);
		assertTrue(organizationService.exists(org1.getId()));
	}
	
	@Test
	void testExistsFalse() {
		when(nodeService.exists(Organization.class, org1.getId())).thenReturn(false);
		assertFalse(organizationService.exists(org1.getId()));
	}
	
	@Test
	void testCreate() {
		UUID id = UUID.randomUUID();
		when(nodeService.create(org1)).thenReturn(org1);
		when(accountSecurityContext.getAuthenticatedAccount()).thenReturn(Optional.of(pa));
		when(pa.getId()).thenReturn(id);
		Organization node = organizationService.create(org1);
		verify(roleAssignmentService).create(org1.getId(), id, new Role.Owner());
		assertEquals(org1, node);
	}
	
	@Test
	void testCreateNotAuth() {
		when(accountSecurityContext.getAuthenticatedAccount()).thenReturn(Optional.empty());

		Throwable exception = assertThrows(NoSuchElementException.class, () -> {
			organizationService.create(org1);
          });
        
        assertEquals("No value present", exception.getMessage());
	}
	
	@Test
	void testDelete() {
		organizationService.delete(org1.getId());
		verify(nodeService).delete(org1.getId());
	}

}
