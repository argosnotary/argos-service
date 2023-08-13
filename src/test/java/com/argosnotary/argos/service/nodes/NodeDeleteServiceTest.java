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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.argosnotary.argos.domain.nodes.Domain;
import com.argosnotary.argos.domain.nodes.Organization;
import com.argosnotary.argos.domain.nodes.Project;
import com.argosnotary.argos.domain.nodes.SupplyChain;
import com.argosnotary.argos.domain.nodes.TreeNode;
import com.argosnotary.argos.service.mongodb.account.ServiceAccountRepository;
import com.argosnotary.argos.service.mongodb.layout.ApprovalConfigurationRepository;
import com.argosnotary.argos.service.mongodb.layout.LayoutMetaBlockRepository;
import com.argosnotary.argos.service.mongodb.layout.ReleaseConfigurationRepository;
import com.argosnotary.argos.service.mongodb.link.LinkMetaBlockRepository;
import com.argosnotary.argos.service.mongodb.nodes.NodeRepository;
import com.argosnotary.argos.service.roles.RoleAssignmentService;

@ExtendWith(MockitoExtension.class)
class NodeDeleteServiceTest {

	@Mock
    private NodeRepository nodeRepository;

	@Mock
	private LayoutMetaBlockRepository layoutMetaBlockRepository;

	@Mock
	private LinkMetaBlockRepository linkMetaBlockRepository;

	@Mock
	private ApprovalConfigurationRepository approvalConfigurationRepository;

	@Mock
	private ServiceAccountRepository serviceAccountRepository;

	@Mock
	private RoleAssignmentService roleAssignmentService;

	@Mock
	private ReleaseConfigurationRepository releaseConfigurationRepository;
	
	@Mock
	private TreeNode node11;
	
	
	
	NodeDeleteService nodeDeleteService;
	
	private TreeNode sc;
	private TreeNode proj;
	
	private TreeNode org1;
	//private ManagementNode node11, node111, node112;

	@BeforeEach
	void setUp() throws Exception {
		nodeDeleteService = new NodeDeleteService(nodeRepository, layoutMetaBlockRepository, linkMetaBlockRepository, approvalConfigurationRepository, serviceAccountRepository, roleAssignmentService, releaseConfigurationRepository);

		
		Organization o = new Organization(UUID.randomUUID(), "org1", Domain.builder().name("org1.com").build());
		Project p = new Project(UUID.randomUUID(), "proj", List.of(), o.getId());
		SupplyChain s = new SupplyChain(UUID.randomUUID(), "sc", List.of(), p.getId());
		
		p.setPathToRoot(List.of(p.getId(), o.getId()));
		s.setPathToRoot(List.of(s.getId(), p.getId(), o.getId()));
		
		org1 = new TreeNode(o, Optional.empty());

		proj = new TreeNode(p, Optional.of(org1));
		sc = new TreeNode(s, Optional.of(proj));
		
		
        
//        node11 = new ManagementNode(UUID.randomUUID(), "node11", org1);
//        node111 = new ManagementNode(UUID.randomUUID(), "node111", node11);
//        node112 = new ManagementNode(UUID.randomUUID(), "node112", node11);
	}

	@Test
	void testDeleteNode() {
		nodeDeleteService.deleteNode(node11);
		verify(node11).visit(nodeDeleteService);
	}

	@Test
	void testVisitEnterSupplyChain() {
		nodeDeleteService.visitEnter(sc);

		verify(layoutMetaBlockRepository).deleteBySupplyChainId(sc.getNode().getId());
		verify(linkMetaBlockRepository).deleteBySupplyChainId(sc.getNode().getId());
		verify(approvalConfigurationRepository).deleteBySupplyChainId(sc.getNode().getId());
		verify(releaseConfigurationRepository).deleteBySupplyChainId(sc.getNode().getId());
		verify(serviceAccountRepository, never()).deleteByProjectId(sc.getNode().getId());
	}

	@Test
	void testVisitEnterProject() {
		nodeDeleteService.visitEnter(proj);

		verify(layoutMetaBlockRepository, never()).deleteBySupplyChainId(proj.getNode().getId());
		verify(linkMetaBlockRepository, never()).deleteBySupplyChainId(proj.getNode().getId());
		verify(approvalConfigurationRepository, never()).deleteBySupplyChainId(proj.getNode().getId());
		verify(releaseConfigurationRepository, never()).deleteBySupplyChainId(proj.getNode().getId());
		verify(serviceAccountRepository).deleteByProjectId(proj.getNode().getId());
	}

	@Test
	void testVisitEnterOther() {
		nodeDeleteService.visitEnter(org1);

		verify(layoutMetaBlockRepository, never()).deleteBySupplyChainId(org1.getNode().getId());
		verify(linkMetaBlockRepository, never()).deleteBySupplyChainId(org1.getNode().getId());
		verify(approvalConfigurationRepository, never()).deleteBySupplyChainId(org1.getNode().getId());
		verify(releaseConfigurationRepository, never()).deleteBySupplyChainId(org1.getNode().getId());
		verify(serviceAccountRepository, never()).deleteByProjectId(org1.getNode().getId());
	}

	@Test
	void testVisitExit() {
		nodeDeleteService.visitExit(org1);
		verify(roleAssignmentService).deleteByResourceId(org1.getNode().getId());
		verify(nodeRepository).deleteById(org1.getNode().getId());
	}

	@Test
	void testVisitEndPoint() {
		Throwable exception = assertThrows(UnsupportedOperationException.class, () -> {
			nodeDeleteService.visitEndPoint(node11);
          });
        
        assertEquals("visitEndPoint method not implemented", exception.getMessage());
	}

}
