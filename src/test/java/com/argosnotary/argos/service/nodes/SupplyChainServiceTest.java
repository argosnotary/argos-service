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
package com.argosnotary.argos.service.nodes;

import static org.junit.jupiter.api.Assertions.*;
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
import com.argosnotary.argos.domain.nodes.Node;
import com.argosnotary.argos.domain.nodes.Organization;
import com.argosnotary.argos.domain.nodes.Project;
import com.argosnotary.argos.domain.nodes.SupplyChain;

@ExtendWith(MockitoExtension.class)
class SupplyChainServiceTest {
	
	private SupplyChainService supplyChainService;
	
	@Mock
	private NodeService nodeService;
	
	private Organization org1;
	private ManagementNode node11, node111, node112;
	private Project proj111, proj1111;
	private SupplyChain sc11111;

	@BeforeEach
	void setUp() throws Exception {
		supplyChainService = new SupplyChainServiceImpl(nodeService);
        org1 = new Organization(UUID.randomUUID(), "org1", Domain.builder().domain("org1.com").build());
        
        node11 = new ManagementNode(UUID.randomUUID(), "node11", List.of(), org1.getId());
        node111 = new ManagementNode(UUID.randomUUID(), "node111", List.of(), node11.getId());
        node112 = new ManagementNode(UUID.randomUUID(), "node112", List.of(), node11.getId());
        
        node11.setPathToRoot(List.of(node11.getId(), org1.getId()));
        node111.setPathToRoot(List.of(node111.getId(), node11.getId(), org1.getId()));
        node112.setPathToRoot(List.of(node112.getId(), node11.getId(), org1.getId()));
        
        proj111 = new Project(UUID.randomUUID(), "proj111", List.of(), node11.getId());
        proj1111 = new Project(UUID.randomUUID(), "proj1111", List.of(), node111.getId());
        
        proj111.setPathToRoot(List.of(proj111.getId(), node11.getId(), org1.getId()));
        proj1111.setPathToRoot(List.of(proj1111.getId(), node111.getId(), node11.getId(), org1.getId()));
        sc11111 = new SupplyChain(UUID.randomUUID(), "sc11111", List.of(), proj1111.getId());
        sc11111.setPathToRoot(List.of(sc11111.getId(), proj111.getId(), node11.getId(), org1.getId()));
	}
	
	@Test
	void testFindNotEmpty() {
		when(nodeService.find(SupplyChain.class.getCanonicalName(), Optional.of(node11))).thenReturn(Set.of(sc11111));
		Set<SupplyChain> nodes = supplyChainService.find(Optional.of(node11));
		assertEquals(Set.of(sc11111), nodes);
	}
	
	@Test
	void testFindEmpty() {
		when(nodeService.find(SupplyChain.class.getCanonicalName(), Optional.empty())).thenReturn(Set.of(sc11111));
		Set<SupplyChain> nodes = supplyChainService.find(Optional.empty());
		assertEquals(Set.of(sc11111), nodes);
	}
	
	@Test
	void testFindById() {
		when(nodeService.findById(sc11111.getId())).thenReturn(Optional.of(sc11111));
		Optional<SupplyChain> node = supplyChainService.findById(sc11111.getId());
		assertEquals(sc11111, node.get());
	}
	
	@Test
	void testFindByIdEmpty() {
		when(nodeService.findById(sc11111.getId())).thenReturn(Optional.empty());
		Optional<SupplyChain> node = supplyChainService.findById(sc11111.getId());
		assertTrue(node.isEmpty());
	}
	
	@Test
	void testFindByIdNotCorrectType() {
		when(nodeService.findById(sc11111.getId())).thenReturn(Optional.of(org1));
		Optional<SupplyChain> node = supplyChainService.findById(sc11111.getId());
		assertTrue(node.isEmpty());
	}
	
	@Test
	void testCreate() {
		when(nodeService.create(sc11111)).thenReturn(sc11111);
		SupplyChain node = supplyChainService.create(sc11111);
		assertEquals(sc11111, node);
	}
	
	@Test
	void testDelete() {
		supplyChainService.delete(sc11111.getId());
		verify(nodeService).delete(sc11111.getId());
	}
	
	@Test
	void testExistsTrue() {
		when(nodeService.exists(SupplyChain.class, sc11111.getId())).thenReturn(true);
		assertTrue(supplyChainService.exists(sc11111.getId()));
	}
	
	@Test
	void testExistsFalse() {
		when(nodeService.exists(SupplyChain.class, sc11111.getId())).thenReturn(false);
		assertFalse(supplyChainService.exists(sc11111.getId()));
	}
	
	@Test
	void testUpdate() {
		when(nodeService.update(sc11111)).thenReturn(sc11111);
		SupplyChain node = supplyChainService.update(sc11111);
		assertEquals(sc11111, node);
	}

}
