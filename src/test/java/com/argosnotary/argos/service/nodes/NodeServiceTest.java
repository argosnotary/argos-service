package com.argosnotary.argos.service.nodes;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
import com.argosnotary.argos.domain.nodes.Node;
import com.argosnotary.argos.domain.nodes.Organization;
import com.argosnotary.argos.domain.nodes.Project;
import com.argosnotary.argos.domain.roles.Permission;
import com.argosnotary.argos.domain.roles.Role;
import com.argosnotary.argos.domain.roles.RoleAssignment;
import com.argosnotary.argos.service.account.AccountSecurityContext;
import com.argosnotary.argos.service.mongodb.nodes.NodeRepository;
import com.argosnotary.argos.service.nodes.NodeDeleteService;
import com.argosnotary.argos.service.nodes.NodeService;
import com.argosnotary.argos.service.nodes.NodeServiceImpl;
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
	@Mock 
	AccountSecurityContext accountSecurityContext;
	
	@Mock
	ServiceAccount sa;
	
	PersonalAccount pa = PersonalAccount.builder().build();
	
    private Organization org1, org2, org3, org4;
    private Project project111, project1111, project1112, project21, project211, project212, project2211, project311, project3111, project3112;
    private ManagementNode node11, node111, node112, node21, node22, node221, node222, node31, node311, node312;
    private Organization org1Mongo;
    private ManagementNode node11Mongo, node111Mongo, node112Mongo;
    private Project project111Mongo, project1111Mongo, project1112Mongo;
    private RoleAssignment ra21, ra22, ra211, ra311;

	@BeforeEach
	void setUp() throws Exception {
		
		nodeService = new NodeServiceImpl(nodeRepository, nodeDeleteService, roleAssignmentService, accountSecurityContext);

        org1 = new Organization(UUID.randomUUID(), "org1", Domain.builder().domain("org1.com").build());
        org2 = new Organization(UUID.randomUUID(), "org2", Domain.builder().domain("org2.com").build());
        org3 = new Organization(UUID.randomUUID(), "org3", Domain.builder().domain("org3.com").build());
        org4 = new Organization(UUID.randomUUID(), "org4", Domain.builder().domain("org4.com").build());
        
        node11 = new ManagementNode(UUID.randomUUID(), "node11", org1);
        node111 = new ManagementNode(UUID.randomUUID(), "node111", node11);
        node112 = new ManagementNode(UUID.randomUUID(), "node112", node11);
        project111 = new Project(UUID.randomUUID(), "project111", node11);
        project1111 = new Project(UUID.randomUUID(), "project1111", node111);
        project1112 = new Project(UUID.randomUUID(), "project1112", node111);
        
        node21 = new ManagementNode(UUID.randomUUID(), "node21", org2);
        node22 = new ManagementNode(UUID.randomUUID(), "node22", org2);
        node221 = new ManagementNode(UUID.randomUUID(), "node221", node22);
        node222 = new ManagementNode(UUID.randomUUID(), "node2211", node22);
        
        node31 = new ManagementNode(UUID.randomUUID(), "node31", org3);
        node311 = new ManagementNode(UUID.randomUUID(), "node311", node31);
        node312 = new ManagementNode(UUID.randomUUID(), "node312", node31);

        project21 = new Project(UUID.randomUUID(), "project21", org2);
        project211 = new Project(UUID.randomUUID(), "project211", node21);
        project212 = new Project(UUID.randomUUID(), "project212", node21);
        project2211 = new Project(UUID.randomUUID(), "project2211", node221);
        
        project311 = new Project(UUID.randomUUID(), "project311", node31);
        project3111 = new Project(UUID.randomUUID(), "project3111", node311);
        project3112 = new Project(UUID.randomUUID(), "project3112", node31);

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
	}

	@Test
	void testGetAllPermissionDownTreeNoAccount() {
		when(accountSecurityContext.getAuthenticatedAccount()).thenReturn(Optional.empty());
		assertThat(nodeService.getAllPermissionDownTree(org2.getId()), is(Set.of()));
	}
	
	@Test
	void testGetAllPermissionDownTreeServiceAccountWrongId() {
		when(accountSecurityContext.getAuthenticatedAccount()).thenReturn(Optional.of(sa));
		when(sa.getProjectId()).thenReturn(project211.getId());
		assertThat(nodeService.getAllPermissionDownTree(org2.getId()), is(Set.of()));
	}
	
	@Test
	void testGetAllPermissionDownTreeServiceAccount() {
		when(accountSecurityContext.getAuthenticatedAccount()).thenReturn(Optional.of(sa));
		when(sa.getProjectId()).thenReturn(project211.getId());
		assertThat(nodeService.getAllPermissionDownTree(project211.getId()), is(Set.of(Permission.LINK_ADD, Permission.RELEASE, Permission.READ)));
	}
	
	@Test
	void testGetAllPermissionDownTreePersonalAccountEmptyIntersect() {
		when(accountSecurityContext.getAuthenticatedAccount()).thenReturn(Optional.of(pa));
		when(nodeRepository.findById(node312.getId())).thenReturn(Optional.of(node312));
		when(roleAssignmentService.findByNodeAndIdentityId(node312, pa.getId())).thenReturn(Set.of());
		assertThat(nodeService.getAllPermissionDownTree(node312.getId()), is(Set.of()));
	}
	
	@Test
	void testGetAllPermissionDownTreePersonalAccount() {
		when(accountSecurityContext.getAuthenticatedAccount()).thenReturn(Optional.of(pa));
		when(nodeRepository.findById(project211.getId())).thenReturn(Optional.of(project211));
		when(roleAssignmentService.findByNodeAndIdentityId(project211, pa.getId())).thenReturn(Set.of(Permission.READ));
		assertThat(nodeService.getAllPermissionDownTree(project211.getId()), is(Set.of(Permission.READ)));
	}
	
	@Test
	void testGetAllPermissionDownTreePersonalAccount2() {
		when(accountSecurityContext.getAuthenticatedAccount()).thenReturn(Optional.of(pa));
		when(nodeRepository.findById(project3111.getId())).thenReturn(Optional.of(project3111));
		when(roleAssignmentService.findByNodeAndIdentityId(project3111, pa.getId())).thenReturn(Set.of(Permission.LINK_ADD, Permission.READ));
		assertThat(nodeService.getAllPermissionDownTree(project3111.getId()), is(Set.of(Permission.LINK_ADD, Permission.READ)));
	}
	
	@Test
	void testSave() {
		when(nodeRepository.findById(node11.getParentId())).thenReturn(Optional.of(org1));
		nodeService.create(node11);
		verify(nodeRepository).save(node11);
	}
	
	@Test
	void testDelete() {
		when(nodeRepository.findInPathToRoot(project3111.getId())).thenReturn(List.of(project3111));
		nodeService.delete(project3111.getId());
		verify(nodeDeleteService).deleteNode(project3111);
	}
	
	@Test
	void testGetSubTree() {
		when(nodeRepository.findInPathToRoot(org1.getId())).thenReturn(List.of(org1Mongo,node11Mongo,node111Mongo,node112Mongo,project111Mongo,project1111Mongo,project1112Mongo));
		Optional<Node> treeNode = nodeService.getSubTree(org1.getId());
		//assertEquals(org1, treeNode.get());
		assertThat(org1.getChildren().size(), is(1));
		assertThat(org1.getChildren().iterator().next().getChildren().size(), is(3));
	}
	
	@Test
	void testGetSubTreeSub() {
		when(nodeRepository.findInPathToRoot(node11Mongo.getId())).thenReturn(List.of(node11Mongo,node111Mongo,node112Mongo,project111Mongo,project1111Mongo,project1112Mongo));
		Optional<Node> treeNode = nodeService.getSubTree(node11Mongo.getId());
		assertEquals(node11, treeNode.get());
		assertThat(node11.getChildren().size(), is(3));
	}
	
	@Test
	void testFindById() {
		when(nodeRepository.findById(project3111.getId())).thenReturn(Optional.of(project3111));
		assertThat( nodeService.findById(project3111.getId()), is(Optional.of(project3111)));
	}
	
	@Test
	void testFind() {
		//fail("Not yet implemented");
	}
	
	@Test
	void testGetFullDomainNameNodeAvailable() {
		when(nodeRepository.findById(project1112.getId())).thenReturn(Optional.of(project1112));
		when(nodeRepository.findWithResourceIds(project1112.getPathToRoot())).thenReturn(List.of(project1112, node11, node111, org1));
		Optional<String> optDomain = nodeService.getFullDomainName(project1112.getId());
		assertEquals(optDomain.get(), "project1112.node111.node11.org1.com");
	}
	
	@Test
	void testGetFullDomainNameNodeNotFound() {
		when(nodeRepository.findById(project1112.getId())).thenReturn(Optional.empty());
		Optional<String> optDomain = nodeService.getFullDomainName(project1112.getId());
		assertEquals(optDomain, Optional.empty());
	}

}
