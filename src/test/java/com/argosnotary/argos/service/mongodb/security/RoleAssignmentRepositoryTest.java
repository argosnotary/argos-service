package com.argosnotary.argos.service.mongodb.security;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.argosnotary.argos.domain.account.PersonalAccount;
import com.argosnotary.argos.domain.account.ServiceAccount;
import com.argosnotary.argos.domain.nodes.Project;
import com.argosnotary.argos.domain.roles.Role;
import com.argosnotary.argos.domain.roles.RoleAssignment;
import com.argosnotary.argos.service.itest.mongodb.ArgosTestContainers;
import com.argosnotary.argos.service.mongodb.roles.RoleAssignmentRepository;

@Testcontainers
@DataMongoTest
class RoleAssignmentRepositoryTest {
	
	@Autowired RoleAssignmentRepository roleAssignmentRepository;

	
	private PersonalAccount pa;
	private ServiceAccount sa;
	
	private UUID resourceId1 = UUID.randomUUID();
	private UUID resourceId2 = UUID.randomUUID();
	private UUID resourceId3 = UUID.randomUUID();
	private UUID resourceId4 = UUID.randomUUID();
	
	private UUID paId = UUID.randomUUID();
	
	private RoleAssignment contributor = RoleAssignment.builder().identityId(paId).resourceId(resourceId4).role(new Role.Contributor()).build();
	private RoleAssignment reader = RoleAssignment.builder().identityId(paId).resourceId(resourceId2).role(new Role.Reader()).build();
	private RoleAssignment adder = RoleAssignment.builder().identityId(paId).resourceId(resourceId2).role(new Role.LinkAdder()).build();
	
	private Project proj1;


	@Container //
	private static MongoDBContainer mongoDBContainer = ArgosTestContainers.getMongoDBContainer();

	@DynamicPropertySource
	static void setProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
	}

	@BeforeEach
	void setUp() throws Exception {
		roleAssignmentRepository.save(contributor);
		roleAssignmentRepository.save(reader);
		roleAssignmentRepository.save(adder);
	}

	@Test
	void testFindByResourceIdsAndAccountId() {
		List<RoleAssignment> ras = roleAssignmentRepository.findByResourceIdsAndIdentityId(List.of(resourceId4), paId);
		assertThat(ras, is(List.of(contributor)));
	}

}
