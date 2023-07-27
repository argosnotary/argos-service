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
package com.argosnotary.argos.service.roles;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
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
import com.argosnotary.argos.service.mongodb.nodes.NodeRepository;
import com.argosnotary.argos.service.mongodb.roles.RoleAssignmentRepository;
import com.argosnotary.argos.service.nodes.NodeDeleteService;
import com.argosnotary.argos.service.nodes.NodeService;
import com.argosnotary.argos.service.nodes.NodeServiceImpl;

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
    private Organization org1Mongo;
    private ManagementNode node11Mongo, node111Mongo, node112Mongo;
    private Project project111Mongo, project1111Mongo, project1112Mongo;
    private RoleAssignment ra21, ra22, ra211, ra311;

	@BeforeEach
	void setUp() throws Exception {
		
		roleAssignmentService = new RoleAssignmentServiceImpl(roleAssignmentRepository, accountSecurityContext);

        org1 = new Organization(UUID.randomUUID(), "org1", Domain.builder().domain("org1.com").build());
        org2 = new Organization(UUID.randomUUID(), "org2", Domain.builder().domain("org2.com").build());
        org3 = new Organization(UUID.randomUUID(), "org3", Domain.builder().domain("org3.com").build());
        
        node11 = new ManagementNode(UUID.randomUUID(), "node11", org1);
        node111 = new ManagementNode(UUID.randomUUID(), "node111", node11);
        node112 = new ManagementNode(UUID.randomUUID(), "node112", node11);
        project111 = new Project(UUID.randomUUID(), "project111", node11);
        project1111 = new Project(UUID.randomUUID(), "project1111", node111);
        project1112 = new Project(UUID.randomUUID(), "project1112", node111);
        
        node21 = new ManagementNode(UUID.randomUUID(), "node21", org2);
        node22 = new ManagementNode(UUID.randomUUID(), "node22", org2);
        
        node31 = new ManagementNode(UUID.randomUUID(), "node31", org3);
        node311 = new ManagementNode(UUID.randomUUID(), "node311", node31);
        node312 = new ManagementNode(UUID.randomUUID(), "node312", node31);

        project211 = new Project(UUID.randomUUID(), "project211", node21);
        
        project3111 = new Project(UUID.randomUUID(), "project3111", node311);
        
        org1Mongo = new Organization();
        org1Mongo.setName("org1");
        org1Mongo.setId(org1.getId());
        org1Mongo.getPathToRoot().add(org1.getId());
        
        List<UUID> pathToRoot = new ArrayList<>(org1Mongo.getPathToRoot());
        
        node11Mongo = new ManagementNode();
        node11Mongo.setName("node11");
        node11Mongo.setId(node11.getId());
        node11Mongo.setParentId(org1.getId());
        pathToRoot.add(0, node11Mongo.getId());
        node11Mongo.setPathToRoot(new ArrayList<>(pathToRoot));
        
        node111Mongo = new ManagementNode();
        node111Mongo.setName("node111");
        node111Mongo.setId(node111.getId());
        node111Mongo.setParentId(node11.getId());
        pathToRoot.add(0, node111Mongo.getId());
        node111Mongo.setPathToRoot(new ArrayList<>(pathToRoot));
        
        node112Mongo = new ManagementNode();
        node112Mongo.setName("node112");
        node112Mongo.setId(node112.getId());
        node112Mongo.setParentId(node11.getId());
        pathToRoot = new ArrayList<>(node11Mongo.getPathToRoot());
        pathToRoot.add(0, node112Mongo.getId());
        node112Mongo.setPathToRoot(new ArrayList<>(pathToRoot));
        
        project111Mongo = new Project();
        project111Mongo.setName("project111");
        project111Mongo.setId(project111.getId());
        project111Mongo.setParentId(node11.getId());
        pathToRoot = new ArrayList<>(node11Mongo.getPathToRoot());
        pathToRoot.add(0, project111Mongo.getId());
        project111Mongo.setPathToRoot(new ArrayList<>(pathToRoot));
        
        project1111Mongo = new Project();
        project1111Mongo.setName("project1111");
        project1111Mongo.setId(project1111.getId());
        project1111Mongo.setParentId(node111.getId());
        pathToRoot = new ArrayList<>(node111Mongo.getPathToRoot());
        pathToRoot.add(0, project1111Mongo.getId());
        project1111Mongo.setPathToRoot(new ArrayList<>(pathToRoot));
        
        project1112Mongo = new Project();
        project1112Mongo.setName("project1112");
        project1112Mongo.setId(project1112.getId());
        project1112Mongo.setParentId(node111.getId());
        pathToRoot = new ArrayList<>(node111Mongo.getPathToRoot());
        pathToRoot.add(0, project1112Mongo.getId());
        project1112Mongo.setPathToRoot(new ArrayList<>(pathToRoot));
        
        ra21 = RoleAssignment.builder().resourceId(node21.getId()).identityId(pa.getId()).role(new Role.Reader()).build();
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
		assertThat(roleAssignmentService.findAllPermissionDownTree(project211), is(Set.of(Permission.LINK_ADD, Permission.RELEASE, Permission.READ)));
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

}