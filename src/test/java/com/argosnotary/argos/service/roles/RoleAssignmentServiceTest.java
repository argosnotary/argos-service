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
package com.argosnotary.argos.service.roles;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.argosnotary.argos.domain.account.PersonalAccount;
import com.argosnotary.argos.domain.account.ServiceAccount;
import com.argosnotary.argos.domain.nodes.Domain;
import com.argosnotary.argos.domain.nodes.ManagementNode;
import com.argosnotary.argos.domain.nodes.Organization;
import com.argosnotary.argos.domain.nodes.Project;
import com.argosnotary.argos.domain.roles.Permission;
import com.argosnotary.argos.domain.roles.Role;
import com.argosnotary.argos.domain.roles.RoleAssignment;
import com.argosnotary.argos.service.account.AccountSecurityContext;
import com.argosnotary.argos.service.mongodb.roles.RoleAssignmentRepository;
import com.argosnotary.argos.service.nodes.NodeDeleteService;
import com.argosnotary.argos.service.nodes.NodeService;

@ExtendWith(MockitoExtension.class)
class RoleAssignmentServiceTest {
	
	RoleAssignmentService roleAssignmentService;

	@Mock
	NodeService nodeService;

	@Mock 
	RoleAssignmentRepository roleAssignmentRepository;
	
	@Mock 
	NodeDeleteService nodeDeleteService;
	
	@Mock 
	AccountSecurityContext accountSecurityContext;
	
	ServiceAccount sa;
	
	PersonalAccount pa = PersonalAccount.builder().build();
	
    private Organization org1, org2, org3;
    private Project project111, project1111, project1112, project211, project3111;
    private ManagementNode node11, node111, node112, node21, node22, node31, node311, node312;
    private RoleAssignment ra21, ra22, ra211, ra311;

	@BeforeEach
	void setUp() throws Exception {
		
		roleAssignmentService = new RoleAssignmentServiceImpl(roleAssignmentRepository, accountSecurityContext);

        org1 = new Organization(UUID.randomUUID(), "org1", Domain.builder().name("org1.com").build());
        org2 = new Organization(UUID.randomUUID(), "org2", Domain.builder().name("org2.com").build());
        org3 = new Organization(UUID.randomUUID(), "org3", Domain.builder().name("org3.com").build());
        
        node11 = new ManagementNode(UUID.randomUUID(), "node11", List.of(), org1.getId());
        node111 = new ManagementNode(UUID.randomUUID(), "node111", List.of(), node11.getId());
        node112 = new ManagementNode(UUID.randomUUID(), "node112", List.of(), node11.getId());        
        node21 = new ManagementNode(UUID.randomUUID(), "node21", new ArrayList<>(), org2.getId());
        node22 = new ManagementNode(UUID.randomUUID(), "node22", new ArrayList<>(), org2.getId());
        node31 = new ManagementNode(UUID.randomUUID(), "node31", new ArrayList<>(), org3.getId());
        node311 = new ManagementNode(UUID.randomUUID(), "node311", new ArrayList<>(), node31.getId());
        node312 = new ManagementNode(UUID.randomUUID(), "node312", new ArrayList<>(), node31.getId());
        
        project111 = new Project(UUID.randomUUID(), "project111", List.of(), node11.getId());
        project1111 = new Project(UUID.randomUUID(), "project1111", List.of(), node111.getId());
        project1112 = new Project(UUID.randomUUID(), "project1112", List.of(), node111.getId());
        project211 = new Project(UUID.randomUUID(), "project211", List.of(), node21.getId());        
        project3111 = new Project(UUID.randomUUID(), "project3111", List.of(), node311.getId());
        
        node11.setPathToRoot(List.of(node11.getId(), org1.getId()));
        node111.setPathToRoot(List.of(node111.getId(), node11.getId(), org1.getId()));
        node112.setPathToRoot(List.of(node112.getId(), node11.getId(), org1.getId()));
        node21.setPathToRoot(List.of(node21.getId(), org2.getId()));
        node22.setPathToRoot(List.of(node22.getId(), org2.getId()));
        node31.setPathToRoot(List.of(node31.getId(), org3.getId()));
        node311.setPathToRoot(List.of(node311.getId(), node31.getId(), org3.getId()));
        node312.setPathToRoot(List.of(node312.getId(), node31.getId(), org3.getId()));
        
        project111.setPathToRoot(List.of(project111.getId(),node11.getId(), org1.getId()));
        project1111.setPathToRoot(List.of(project1111.getId(),node111.getId(),node11.getId(), org1.getId()));
        project1112.setPathToRoot(List.of(project1112.getId(),node111.getId(),node11.getId(), org1.getId()));
        project211.setPathToRoot(List.of(project211.getId(),node21.getId(), org2.getId()));
        project3111.setPathToRoot(List.of(project3111.getId(),node311.getId(), node31.getId(), org3.getId()));
        
        ra21 = RoleAssignment.builder().id(UUID.randomUUID()).resourceId(node21.getId()).identityId(pa.getId()).role(new Role.Reader()).build();
        ra22 = RoleAssignment.builder().resourceId(node22.getId()).identityId(pa.getId()).role(new Role.Reader()).build();
        ra211 = RoleAssignment.builder().resourceId(project211.getId()).identityId(pa.getId()).role(new Role.Reader()).build();
        ra311 = RoleAssignment.builder().resourceId(node311.getId()).identityId(pa.getId()).role(new Role.LinkAdder()).build();
		
		sa = ServiceAccount.builder().name("sa").projectId(project211.getId()).build();
	}



	@Test
	void testGetAllPermissionDownTreeNoAccount() {
		when(accountSecurityContext.getAuthenticatedAccount()).thenReturn(Optional.empty());
		assertThat(roleAssignmentService.findAllPermissionDownTree(org2), is(Set.of()));
	}
	
	@Test
	void testGetAllPermissionDownTreeServiceAccountWrongId() {
		when(accountSecurityContext.getAuthenticatedAccount()).thenReturn(Optional.of(sa));
		assertThat(roleAssignmentService.findAllPermissionDownTree(org2), is(Set.of()));
	}
	
	@Test
	void testGetAllPermissionDownTreeServiceAccount() {
		when(accountSecurityContext.getAuthenticatedAccount()).thenReturn(Optional.of(sa));
		assertThat(roleAssignmentService.findAllPermissionDownTree(project211), is(Set.of(Permission.LINK_ADD, Permission.ATTESTATION_ADD, Permission.RELEASE, Permission.READ)));
	}
	
	@Test
	void testGetAllPermissionDownTreePersonalAccountEmptyIntersect() {
		when(accountSecurityContext.getAuthenticatedAccount()).thenReturn(Optional.of(pa));
		when(roleAssignmentRepository.findByResourceIdsAndIdentityId(node312.getPathToRoot(), pa.getId())).thenReturn(List.of());
		assertThat(roleAssignmentService.findAllPermissionDownTree(node312), is(Set.of()));
	}
	
	@Test
	void testGetAllPermissionDownTreePersonalAccount() {
		when(accountSecurityContext.getAuthenticatedAccount()).thenReturn(Optional.of(pa));
		when(roleAssignmentRepository.findByResourceIdsAndIdentityId(project211.getPathToRoot(), pa.getId())).thenReturn(List.of(ra211));
		assertThat(roleAssignmentService.findAllPermissionDownTree(project211), is(Set.of(Permission.READ)));
	}
	
	@Test
	void testGetAllPermissionDownTreePersonalAccount2() {
		when(accountSecurityContext.getAuthenticatedAccount()).thenReturn(Optional.of(pa));
		when(roleAssignmentRepository.findByResourceIdsAndIdentityId(project3111.getPathToRoot(), pa.getId())).thenReturn(List.of(ra311));
		assertThat(roleAssignmentService.findAllPermissionDownTree(project3111), is(Set.of(Permission.LINK_ADD, Permission.READ)));
	}
	
	@Test
	void testSave() {
		when(roleAssignmentRepository.save(ra21)).thenReturn(ra21);
		RoleAssignment ra = roleAssignmentService.save(ra21);
		verify(roleAssignmentRepository).save(ra21);
		assertEquals(ra21, ra);
		
	}
	
	@Test
	void testSaveIdNull() {

		RoleAssignment ra = roleAssignmentService.save(ra22);
		verify(roleAssignmentRepository).save(any());
		
	}

	@Test
	void testDelete() {
		UUID id = UUID.randomUUID();
		roleAssignmentService.delete(id);
		verify(roleAssignmentRepository).deleteById(id);
	}

	@Test
	void testDeleteByResourceId() {
		UUID resourceId = UUID.randomUUID();
		roleAssignmentService.deleteByResourceId(resourceId);
		verify(roleAssignmentRepository).deleteByResourceId(resourceId);
		
	}

	@Test
	void testFindByResourceId() {
		when(roleAssignmentRepository.findByResourceId(ra21.getResourceId())).thenReturn(List.of(ra21));
		List<RoleAssignment> rl = roleAssignmentService.findByResourceId(ra21.getResourceId());
		assertEquals(List.of(ra21), rl);
	}

	@Test
	void testCreate() {
		RoleAssignment exp = RoleAssignment.builder().id(UUID.randomUUID()).resourceId(node21.getId()).identityId(pa.getId()).role(new Role.Reader()).build();
		when(roleAssignmentRepository.save(any())).thenReturn(exp);
		RoleAssignment act = roleAssignmentService.create(node21.getId(), pa.getId(), new Role.Reader());
		verify(roleAssignmentRepository).save(any());
		assertEquals(exp.getIdentityId(), act.getIdentityId());
		assertEquals(exp.getResourceId(), act.getResourceId());
		assertEquals(exp.getRole(), act.getRole());
	}

	@Test
	void testFindByIdentity() {
		when(accountSecurityContext.getAuthenticatedAccount()).thenReturn(Optional.of(pa));
		when(roleAssignmentRepository.findByIdentityId(pa.getId())).thenReturn(List.of(ra22));
		List<RoleAssignment> rl = roleAssignmentService.findByIdentity();
		assertEquals(List.of(ra22), rl);
		
	}

	@Test
	void testFindByIdentityNotAuth() {
		when(accountSecurityContext.getAuthenticatedAccount()).thenReturn(Optional.empty());
		List<RoleAssignment> rl = roleAssignmentService.findByIdentity();
		assertTrue(rl.isEmpty());
		
	}

}
