package com.argosnotary.argos.domain.account;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.argosnotary.argos.domain.crypto.KeyPair;
import com.argosnotary.argos.domain.roles.Role;
import com.argosnotary.argos.domain.roles.RoleAssignment;

class ServiceAccountTest {

	@BeforeEach
	void setUp() throws Exception {
	}

	@Test
	void testConstructor() {
		UUID projectId = UUID.randomUUID();
		RoleAssignment releaser = RoleAssignment.builder().resourceId(projectId).role(new Role.Releaser()).build();
		RoleAssignment la = RoleAssignment.builder().resourceId(projectId).role(new Role.LinkAdder()).build();
		
		ServiceAccount sa = ServiceAccount.builder().name("sa").projectId(projectId).build();
	}
	
	@Test
	void testResourceId() {
		UUID aId = UUID.randomUUID();
		ServiceAccount sa = ServiceAccount.builder().name("sa").id(aId).build();
		assertThat(sa.getId()).isEqualTo(aId);
	}
	

	
	@Test
	void testInActiveKeys() {
		UUID aId = UUID.randomUUID();
		KeyPair kp = new KeyPair("arg1", "arg2".getBytes(), "arg3".getBytes());
		ServiceAccount sa = ServiceAccount.builder().name("sa").id(aId).inactiveKeyPairs(Set.of(kp)).build();
		assertThat(sa.getInactiveKeyPairs()).isEqualTo(Set.of(kp));
		sa = ServiceAccount.builder().name("sa").id(aId).inactiveKeyPairs(Set.of()).build();
		assertThat(sa.getInactiveKeyPairs()).isEmpty();
	}

}
