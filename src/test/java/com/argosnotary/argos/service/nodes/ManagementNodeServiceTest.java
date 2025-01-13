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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.argosnotary.argos.domain.nodes.Domain;
import com.argosnotary.argos.domain.nodes.ManagementNode;
import com.argosnotary.argos.domain.nodes.Organization;

@ExtendWith(MockitoExtension.class)
class ManagementNodeServiceTest {
	
	ManagementNodeService managementNodeService;
	
	@Mock
	NodeService nodeService;
	
	private Organization org1;
	private ManagementNode node11, node111, node112;

	@BeforeEach
	void setUp() throws Exception {
		managementNodeService = new ManagementNodeServiceImpl(nodeService);
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
		when(nodeService.find(ManagementNode.class.getCanonicalName(), Optional.of(org1))).thenReturn(Set.of(node11,node111,node112));
		Set<ManagementNode> nodes = managementNodeService.find(Optional.of(org1));
		assertEquals(Set.of(node11,node111,node112), nodes);
	}
	
	@Test
	void testFindEmpty() {
		when(nodeService.find(ManagementNode.class.getCanonicalName(), Optional.empty())).thenReturn(Set.of(node11,node111,node112));
		Set<ManagementNode> nodes = managementNodeService.find(Optional.empty());
		assertEquals(Set.of(node11,node111,node112), nodes);
	}
	
	@Test
	void testFindById() {
		when(nodeService.findById(node11.getId())).thenReturn(Optional.of(node11));
		Optional<ManagementNode> node = managementNodeService.findById(node11.getId());
		assertEquals(node11, node.get());
	}
	
	@Test
	void testFindByIdEmpty() {
		when(nodeService.findById(node11.getId())).thenReturn(Optional.empty());
		Optional<ManagementNode> node = managementNodeService.findById(node11.getId());
		assertTrue(node.isEmpty());
	}
	
	@Test
	void testFindByIdNotCorrectType() {
		when(nodeService.findById(node11.getId())).thenReturn(Optional.of(org1));
		Optional<ManagementNode> node = managementNodeService.findById(node11.getId());
		assertTrue(node.isEmpty());
	}
	
	@Test
	void testExistsTrue() {
		when(nodeService.exists(ManagementNode.class, node11.getId())).thenReturn(true);
		assertTrue(managementNodeService.exists(node11.getId()));
	}
	
	@Test
	void testExistsFalse() {
		when(nodeService.exists(ManagementNode.class, node11.getId())).thenReturn(false);
		assertFalse(managementNodeService.exists(node11.getId()));
	}
	
	@Test
	void testCreate() {
		when(nodeService.create(node11)).thenReturn(node11);
		ManagementNode node = managementNodeService.create(node11);
		verify(nodeService).create(node11);
		assertEquals(node11, node);
	}
	
	@Test
	void testDelete() {
		managementNodeService.delete(node11.getId());
		verify(nodeService).delete(node11.getId());
	}

}
