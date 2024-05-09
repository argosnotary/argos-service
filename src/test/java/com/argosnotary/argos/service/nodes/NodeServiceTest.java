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
package com.argosnotary.argos.service.nodes;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.argosnotary.argos.domain.ArgosError;
import com.argosnotary.argos.domain.account.PersonalAccount;
import com.argosnotary.argos.domain.account.ServiceAccount;
import com.argosnotary.argos.domain.nodes.Domain;
import com.argosnotary.argos.domain.nodes.ManagementNode;
import com.argosnotary.argos.domain.nodes.Node;
import com.argosnotary.argos.domain.nodes.Organization;
import com.argosnotary.argos.domain.nodes.Project;
import com.argosnotary.argos.domain.nodes.TreeNode;
import com.argosnotary.argos.domain.roles.Permission;
import com.argosnotary.argos.domain.roles.Role;
import com.argosnotary.argos.domain.roles.RoleAssignment;
import com.argosnotary.argos.service.mongodb.nodes.NodeRepository;
import com.argosnotary.argos.service.roles.RoleAssignmentService;

@ExtendWith(MockitoExtension.class)
class NodeServiceTest {
	
	NodeService nodeService;

	@Mock 
	NodeRepository nodeRepository;
	
	@Mock 
	NodeDeleteService nodeDeleteService;
	@Mock 
	RoleAssignmentService roleAssignmentService;
	
	ServiceAccount sa2111;
	
	PersonalAccount pa = PersonalAccount.builder().build();
	
    private Organization org1, org2, org3;
    private Project project111, project1111, project1112, project211, project3111;
    private ManagementNode node11, node111, node112, node21, node22, node221, node31, node311;
    private RoleAssignment ra11, ra21, ra22, ra211, ra311;

	@BeforeEach
	void setUp() throws Exception {
		
		// org1 - node11 - project111
		//               - node111    - project1111
		//                            - project1112
		//               - node112
		// org2 - node21 - project211 - sa
		//      - node22 - node221
		// org3 - node31 - node311    - project3111
		
		nodeService = new NodeServiceImpl(nodeRepository, nodeDeleteService, roleAssignmentService);

        org1 = new Organization(UUID.randomUUID(), "org1", Domain.builder().name("org1.com").build());
        org2 = new Organization(UUID.randomUUID(), "org2", Domain.builder().name("org2.com").build());
        org3 = new Organization(UUID.randomUUID(), "org3", Domain.builder().name("org3.com").build());
        
        node11 = new ManagementNode(UUID.randomUUID(), "node11", new ArrayList<>(), org1.getId());
        node111 = new ManagementNode(UUID.randomUUID(), "node111", new ArrayList<>(), node11.getId());
        node112 = new ManagementNode(UUID.randomUUID(), "node112", new ArrayList<>(), node11.getId());
        node21 = new ManagementNode(UUID.randomUUID(), "node21", new ArrayList<>(), org2.getId());
        node22 = new ManagementNode(UUID.randomUUID(), "node22", new ArrayList<>(), org2.getId());
        node221 = new ManagementNode(UUID.randomUUID(), "node221", new ArrayList<>(), node22.getId());
        node31 = new ManagementNode(UUID.randomUUID(), "node21", new ArrayList<>(), org3.getId());
        node311 = new ManagementNode(UUID.randomUUID(), "node22", new ArrayList<>(), node31.getId());
        
        project111 = new Project(UUID.randomUUID(), "project111", List.of(), node11.getId());
        project1111 = new Project(UUID.randomUUID(), "project1111", List.of(), node111.getId());
        project1112 = new Project(UUID.randomUUID(), "project1112", List.of(), node111.getId());
        project211 = new Project(UUID.randomUUID(), "project211", List.of(), node21.getId());
        project3111 = new Project(UUID.randomUUID(), "project3111", List.of(), node311.getId());
        
		sa2111 = ServiceAccount.builder().name("sa2111").projectId(project211.getId()).build();
        
        node11.setPathToRoot(List.of(node11.getId(), org1.getId()));
        node111.setPathToRoot(List.of(node111.getId(), node11.getId(), org1.getId()));
        node112.setPathToRoot(List.of(node112.getId(), node11.getId(), org1.getId()));
        node21.setPathToRoot(List.of(node21.getId(), org2.getId()));
        node22.setPathToRoot(List.of(node22.getId(), org2.getId()));
        node221.setPathToRoot(List.of(node221.getId(), node22.getId(), org2.getId()));
        node31.setPathToRoot(List.of(node31.getId(), org3.getId()));
        node311.setPathToRoot(List.of(node311.getId(), node31.getId(), org3.getId()));
        
        project111.setPathToRoot(List.of(project111.getId(), node11.getId(), org1.getId()));
        project1111.setPathToRoot(List.of(project1111.getId(),node111.getId(), node11.getId(), org1.getId()));
        project1112.setPathToRoot(List.of(project1112.getId(),node111.getId(), node11.getId(), org1.getId()));
        project211.setPathToRoot(List.of(project211.getId(),node21.getId(), org2.getId()));
        project3111.setPathToRoot(List.of(project3111.getId(),node311.getId(), node31.getId(), org3.getId()));
        
		sa2111 = ServiceAccount.builder().name("sa2111").projectId(project211.getId()).build();
        
        ra11 = RoleAssignment.builder().resourceId(node11.getId()).identityId(pa.getId()).role(new Role.Reader()).build();
        ra21 = RoleAssignment.builder().resourceId(node21.getId()).identityId(pa.getId()).role(new Role.Reader()).build();
        ra22 = RoleAssignment.builder().resourceId(node22.getId()).identityId(pa.getId()).role(new Role.Reader()).build();
        ra211 = RoleAssignment.builder().resourceId(project211.getId()).identityId(pa.getId()).role(new Role.Reader()).build();
        ra311 = RoleAssignment.builder().resourceId(node311.getId()).identityId(pa.getId()).role(new Role.LinkAdder()).build();
	}
	
	@Test
	void testCreate() {
		when(nodeRepository.findById(node11.getParentId())).thenReturn(Optional.of(org1));
		when(nodeRepository.insert(node11)).thenReturn(node11);
		Node node = nodeService.create(node11);
		verify(nodeRepository).insert(node11);
		assertEquals(node11, node);
	}
	
	@Test
	void testCreateOrganization() {
		when(nodeRepository.insert(org1)).thenReturn(org1);
		Node org = nodeService.create(org1);
		verify(nodeRepository).insert(org1);
		assertEquals(org1, org);
	}
	
	@Test
	void testCreateNoId() {
		ManagementNode mn = new ManagementNode(null, "node", List.of(), org1.getId());
		mn.setId(null);
		mn.setParentId(org1.getId());
		when(nodeRepository.findById(mn.getParentId())).thenReturn(Optional.of(org1));
		when(nodeRepository.insert(mn)).thenReturn(mn);
		Node node = nodeService.create(mn);
		verify(nodeRepository).insert(mn);
		assertEquals(org1.getId(), mn.getParentId());
		assertEquals(List.of(node.getId(), org1.getId()), node.getPathToRoot());
	}
	
	@Test
	void testUpdate() {
		when(nodeRepository.findById(node11.getParentId())).thenReturn(Optional.of(org1));
		when(nodeRepository.save(node11)).thenReturn(node11);
		when(nodeRepository.existsById(node11.getId())).thenReturn(true);
		Node node = nodeService.update(node11);
		verify(nodeRepository).save(node11);
		assertEquals(node11, node);
	}
	
	@Test
	void testUpdateNotExist() {
		when(nodeRepository.existsById(node11.getId())).thenReturn(false);
		Throwable exception = assertThrows(ArgosError.class, () -> {
			nodeService.update(node11);
          });
        assertEquals("ManagementNode doesn't exist", exception.getMessage());
		verify(nodeRepository, never()).save(node11);
	}
	
	@Test
	void testUpdateNull() {
		node11.setId(null);
		Throwable exception = assertThrows(ArgosError.class, () -> {
			nodeService.update(node11);
          });
        assertEquals("ManagementNode doesn't exist", exception.getMessage());
		verify(nodeRepository, never()).save(node11);
	}
	
	@Test
	void testDelete() {
		when(nodeRepository.findByPathToRoot(project211.getId())).thenReturn(List.of(project211));
		TreeNode node = TreeNode.createUpTree(Set.of(project211));
		nodeService.delete(project211.getId());
		verify(nodeDeleteService).deleteNode(node);
	}
	
	@Test
	void testFindById() {
		when(nodeRepository.findById(project3111.getId())).thenReturn(Optional.of(project3111));
		assertThat( nodeService.findById(project3111.getId()), is(Optional.of(project3111)));
	}
	
	@Test
	void testFindWithNode() {
		when(roleAssignmentService.findAllPermissionDownTree(node111)).thenReturn(Set.of(Permission.READ));
		when(nodeRepository.findWithClassAndResourceIdsUpTree(Project.class.getCanonicalName(), Set.of(node111.getId())))
			.thenReturn(List.of(project1111, project1112));
		when(nodeRepository.findWithIds(new HashSet<>(node111.getPathToRoot()))).thenReturn(List.of(org1, node11, node111));
		when(nodeRepository.findWithClassAndResourceIds(Project.class.getCanonicalName(), Set.of(org1.getId(), node11.getId(), node111.getId()))).thenReturn(List.of());
		Set<Node> nodes = nodeService.find(Project.class.getCanonicalName(), Optional.of(node111));
		assertEquals(Set.of(project1111, project1112), nodes);
	}
	
	@Test
	void testFindWithNodeNoPermission() {
		when(roleAssignmentService.findAllPermissionDownTree(node111)).thenReturn(Set.of());
		when(nodeRepository.findWithClassAndResourceIdsUpTree(Project.class.getCanonicalName(), Set.of()))
			.thenReturn(List.of());
		when(nodeRepository.findWithIds(new HashSet<>())).thenReturn(List.of());
		when(nodeRepository.findWithClassAndResourceIds(Project.class.getCanonicalName(), Set.of())).thenReturn(List.of());
		Set<Node> nodes = nodeService.find(Project.class.getCanonicalName(), Optional.of(node111));
		assertEquals(Set.of(), nodes);
	}
	
	@Test
	void testFindWithoutNode() {
		when(roleAssignmentService.findByIdentity()).thenReturn(List.of(ra11, ra311));
		when(nodeRepository.findWithIds(Set.of(ra11.getResourceId(), ra311.getResourceId()))).thenReturn(List.of(node11, node311));
		when(nodeRepository.findWithClassAndResourceIdsUpTree(Project.class.getCanonicalName(), Set.of(node11.getId(), node311.getId())))
			.thenReturn(List.of(project111, project1111, project1112, project3111));
		Set<UUID> pathIds = new HashSet<>(node11.getPathToRoot());
		pathIds.addAll(node311.getPathToRoot());
		when(nodeRepository.findWithIds(pathIds)).thenReturn(List.of(org1, node11, org3, node31, node311));
		when(nodeRepository.findWithClassAndResourceIds(Project.class.getCanonicalName(), Set.of(org1.getId(), node11.getId(), org3.getId(), node31.getId(), node311.getId()))).thenReturn(List.of());
		Set<Node> nodes = nodeService.find(Project.class.getCanonicalName(), Optional.empty());
		assertEquals(new HashSet<>(Set.of(project111, project1111, project1112, project3111)), nodes);
	}
	
	@Test
	void testFindWithoutNodeManagementNode() {
		when(roleAssignmentService.findByIdentity()).thenReturn(List.of(ra11, ra311));
		when(nodeRepository.findWithIds(Set.of(ra11.getResourceId(), ra311.getResourceId()))).thenReturn(List.of(node11, node311));
		when(nodeRepository.findWithClassAndResourceIdsUpTree(ManagementNode.class.getCanonicalName(), Set.of(node11.getId(), node311.getId())))
			.thenReturn(List.of(node11, node111, node112, node311));
		Set<UUID> pathIds = new HashSet<>(node11.getPathToRoot());
		pathIds.addAll(node311.getPathToRoot());
		when(nodeRepository.findWithIds(pathIds)).thenReturn(List.of(org1, node11, org3, node31, node311));
		when(nodeRepository.findWithClassAndResourceIds(ManagementNode.class.getCanonicalName(), Set.of(org1.getId(), node11.getId(), org3.getId(), node31.getId(), node311.getId())))
		.thenReturn(List.of(node11, org3, node31, node311));
		Set<Node> nodes = nodeService.find(ManagementNode.class.getCanonicalName(), Optional.empty());
		assertEquals(new HashSet<>(Set.of(node11, node111, node112, org3, node31, node311)), nodes);
	}
	

	
	@Test
	void testFindWithoutNodeNoPermission() {
		when(roleAssignmentService.findByIdentity()).thenReturn(List.of());
		when(nodeRepository.findWithIds(Set.of())).thenReturn(List.of());
		when(nodeRepository.findWithClassAndResourceIdsUpTree(ManagementNode.class.getCanonicalName(), Set.of()))
			.thenReturn(List.of());
		Set<UUID> pathIds = new HashSet<>();
		when(nodeRepository.findWithIds(pathIds)).thenReturn(List.of());
		when(nodeRepository.findWithClassAndResourceIds(ManagementNode.class.getCanonicalName(), Set.of()))
		.thenReturn(List.of());
		Set<Node> nodes = nodeService.find(ManagementNode.class.getCanonicalName(), Optional.empty());
		assertEquals(new HashSet<>(), nodes);
	}
	
	@Test
	void testExistsByClassAndId() {
		UUID id = UUID.randomUUID();
        when(nodeRepository.existsByClassAndId(Organization.class.getCanonicalName(), id)).thenReturn(true);
        assertTrue(nodeService.exists(Organization.class, id));

        when(nodeRepository.existsByClassAndId(Project.class.getCanonicalName(), id)).thenReturn(false);
        assertFalse(nodeService.exists(Project.class, id));
    }
    
	@Test
	void testExistsByClassAndName() {

		UUID id = UUID.randomUUID();
        when(nodeRepository.existsByClassAndName(Organization.class.getCanonicalName(), "org")).thenReturn(true);
        assertTrue(nodeService.exists(Organization.class, "org"));

        when(nodeRepository.existsByClassAndName(Project.class.getCanonicalName(), "proj")).thenReturn(false);
        assertFalse(nodeService.exists(Project.class, "proj"));
    }
	
	@Test
	void testGetQualifiedNameNodeAvailable() {
		when(nodeRepository.findById(project1112.getId())).thenReturn(Optional.of(project1112));
		when(nodeRepository.findAllById(project1112.getPathToRoot())).thenReturn(List.of(project1112,node111, node11, org1));
		Optional<String> optDomain = nodeService.getQualifiedName(project1112.getId());
		assertEquals("com.org1.node11.node111.project1112", optDomain.get());
	}
	
	@Test
	void testGetQualifiedNameNodeNotFound() {
		when(nodeRepository.findById(project1112.getId())).thenReturn(Optional.empty());
		Optional<String> optDomain = nodeService.getQualifiedName(project1112.getId());
		assertEquals(optDomain, Optional.empty());
	}
	
	@Test
	void testFindOrganizationInPath() {
		when(nodeRepository.findById(project1112.getId())).thenReturn(Optional.of(project1112));
		when(nodeRepository.findById(org1.getId())).thenReturn(Optional.of(org1));
		Organization org = nodeService.findOrganizationInPath(project1112.getId());
		assertEquals(org1, org);
		
	}
	
	@Test
	void testExistsByParentIdAndNameExist() {
		when(nodeRepository.existsByParentIdAndName(project1112.getParentId(), project1112.getName())).thenReturn(true);
		assertTrue(nodeService.existsByParentIdAndName(project1112.getParentId(), project1112.getName()));
		
		when(nodeRepository.existsByParentIdAndName(project1111.getParentId(), project1111.getName())).thenReturn(false);
		assertFalse(nodeService.existsByParentIdAndName(project1111.getParentId(), project1111.getName()));
		
	}

}
