package com.argosnotary.argos.service.mongodb.nodes;


import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIn.in;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.argosnotary.argos.domain.nodes.Domain;
import com.argosnotary.argos.domain.nodes.ManagementNode;
import com.argosnotary.argos.domain.nodes.Node;
import com.argosnotary.argos.domain.nodes.Organization;
import com.argosnotary.argos.domain.nodes.Project;
import com.argosnotary.argos.service.itest.mongodb.ArgosTestContainers;

@Testcontainers
@DataMongoTest
class NodeRepositoryTest {
	private final UUID ORGANIZATION_ID = UUID.randomUUID();

	@Container //
	private static MongoDBContainer mongoDBContainer = ArgosTestContainers.getMongoDBContainer();

	@DynamicPropertySource
	static void setProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
	}

	@Autowired NodeRepository nodeRepository;
	
    private Organization org1, org2, org3, org4;
    private Project project21, project211, project212, project2211, project311, project3111, project3112;
    private ManagementNode node21, node22, node221, node222, node31, node311, node312;
    

    private List<Organization> list1, list2;

	@BeforeEach
	void setUp() {

        org1 = new Organization(UUID.randomUUID(), "org1", null);
        org2 = new Organization(UUID.randomUUID(), "org2", null);
        org3 = new Organization(UUID.randomUUID(), "org3", null);
        org4 = new Organization(UUID.randomUUID(), "org4", null);
        
        org2.setDomain(Domain.builder().build());
        
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

        nodeRepository.deleteAll();


        nodeRepository.save(project21);
        nodeRepository.save(project211);
        nodeRepository.save(project212);
        nodeRepository.save(project2211);
        nodeRepository.save(project311);
        nodeRepository.save(project3111);
        nodeRepository.save(project3112);
        nodeRepository.save(node21);
        nodeRepository.save(node22);
        nodeRepository.save(node221);
        nodeRepository.save(node222);
        nodeRepository.save(node31);
        nodeRepository.save(node311);
        nodeRepository.save(node312);
		this.org2 = nodeRepository.save(org2);
        this.org1 = nodeRepository.save(org1);
		this.org3 = nodeRepository.save(org3);
        this.org4 = nodeRepository.save(org4); 
	}
	
	@Test
	void testParentIdNull() {

	}

	/**
	 * @see #153
	 */
	@Test
	void countByConcreteSubtypeExample() {

		List<Node> nodes = nodeRepository.findAll();
		assertThat(org1, is(in(nodes)));
		assertThat(org2, is(in(nodes)));
		assertThat(nodes.size(), is(18));
	}
	
	@Test
	void existsByClassAndName() {
		assertTrue(nodeRepository.existsByClassAndName(Organization.class.getCanonicalName(), "org1"));
		assertFalse(nodeRepository.existsByClassAndName(Organization.class.getCanonicalName(), "bla"));
		
	}
	
	@Test
	void existsByClassAndId() {
		assertTrue(nodeRepository.existsByClassAndId(Organization.class.getCanonicalName(), org1.getId()));
		assertFalse(nodeRepository.existsByClassAndId(Organization.class.getCanonicalName(), project21.getId()));
		
	}
	
	@Test
	void findByPathToRoot() {
		List<Node> nodes = nodeRepository.findByPathToRoot(node22.getId());
		assertThat(nodes, containsInAnyOrder(node22,node221, node222, project2211));
		
	}
	
	@Test
	void findPathToRootByIdTest() {
		Optional<Node> optNodes = nodeRepository.findById(project2211.getId());
		assertEquals(optNodes.get().getPathToRoot(), List.of(project2211.getId(), node221.getId(), node22.getId(), org2.getId()));
		
	}
	
	@Test
	void testFindWithClassAndResourceIdsUpTree() {
		List<Node> upTreeNodes = nodeRepository.findWithClassAndResourceIdsUpTree(Project.class.getCanonicalName(), Set.of(org1.getId()));
		
		assertThat(upTreeNodes, is(List.of()));
		
		upTreeNodes = nodeRepository.findWithClassAndResourceIdsUpTree(Project.class.getCanonicalName(), Set.of(org2.getId()));
		
		assertThat(upTreeNodes.size(), is(4));
		assertThat(upTreeNodes, containsInAnyOrder(project21,project211,project212,project2211));
		
		upTreeNodes = nodeRepository.findWithClassAndResourceIdsUpTree(Project.class.getCanonicalName(), Set.of(org2.getId(),org3.getId()));
		
		assertThat(upTreeNodes.size(), is(7));
		assertThat(upTreeNodes, containsInAnyOrder(project21,project211,project212,project2211,project311,project3111,project3112));
		
	}
	

	
	@Test
	void testFindWithIds() {
		List<Node> nodes = nodeRepository.findWithIds(Set.of(org1.getId()));
		
		assertThat(nodes.size(), is(1));
		assertThat(nodes, containsInAnyOrder(org1));
		
		nodes = nodeRepository.findWithIds(Set.of(org2.getId(),org3.getId()));
		
		assertThat(nodes.size(), is(2));
		assertThat(nodes, containsInAnyOrder(org2,org3));
		
	}
	
	@Test
	void testFindWithClassAndResourceIds() {

		
		Set<UUID> pathUuids = nodeRepository.findWithIds(Set.of(project2211.getId())).stream()
				.map(n -> n.getPathToRoot()).flatMap(List::stream).collect(Collectors.toSet());
		
		List<Node> nodes = nodeRepository.findWithClassAndResourceIds(Organization.class.getCanonicalName(), pathUuids);
		
		assertThat(nodes, is(List.of(org2)));
		
		nodes = nodeRepository.findWithClassAndResourceIdsUpTree(Project.class.getCanonicalName(), Set.of(org2.getId()));
		
		pathUuids = nodeRepository.findWithIds(Set.of(project2211.getId(), project3112.getId())).stream()
				.map(n -> n.getPathToRoot()).flatMap(List::stream).collect(Collectors.toSet());
		
		nodes = nodeRepository.findWithClassAndResourceIds(Organization.class.getCanonicalName(), pathUuids);
		
		assertThat(nodes.size(), is(2));
		assertThat(nodes, containsInAnyOrder(org2,org3));
		
	}
	
	@Test
	void findProjectsByOrganizationIds() {
		
		Set<Node> result = findNodesOfClasses(Project.class.getCanonicalName(), Set.of(org1.getId()));
		
		assertThat(result, is(Set.of()));
		
		result = findNodesOfClasses(Project.class.getCanonicalName(), Set.of(org2.getId()));
		
		assertThat(result.size(), is(4));
		assertThat(result, containsInAnyOrder(project21,project211,project212,project2211));
		
		result = findNodesOfClasses(Project.class.getCanonicalName(), Set.of(org1.getId(), org2.getId()));
		
		assertThat(result.size(), is(4));
		assertThat(result, containsInAnyOrder(project21,project211,project212,project2211));
		
		result = findNodesOfClasses(Project.class.getCanonicalName(), Set.of(org2.getId(), org3.getId()));
		
		assertThat(result.size(), is(7));
		assertThat(result, containsInAnyOrder(project21,project211,project212,project2211,project311,project3111,project3112));
		
	}
	
	@Test
	void findOrganizationsByResourceIdsTest() {
		Set<Node> result = findNodesOfClasses(Organization.class.getCanonicalName(), Set.of(org1.getId()));
		
		assertThat(result, is(Set.of(org1)));
		
		result = findNodesOfClasses(Organization.class.getCanonicalName(), Set.of(project2211.getId()));
		
		assertThat(result, is(Set.of(org2)));
		
		result = findNodesOfClasses(Organization.class.getCanonicalName(), Set.of(project2211.getId(), project3112.getId()));
		
		assertThat(result, is(Set.of(org2, org3)));
	}
	
	private Set<Node> findNodesOfClasses(String clazz, Set<UUID> ids) {
		Set<Node> result = new HashSet<>();
		List<Node> upTreeNodes = nodeRepository.findWithClassAndResourceIdsUpTree(clazz, ids);
		
		Set<UUID> pathUuids = nodeRepository.findWithIds(ids).stream()
				.map(n -> n.getPathToRoot()).flatMap(List::stream).collect(Collectors.toSet());
		
		List<Node> pathNodes = nodeRepository.findWithClassAndResourceIds(clazz, pathUuids);
		
		result.addAll(upTreeNodes);
		result.addAll(pathNodes);
		
		return result;
		
	}

}
