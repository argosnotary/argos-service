package com.argosnotary.argos.domain.permission;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.argosnotary.argos.domain.roles.Role.Releaser;
import com.argosnotary.argos.domain.roles.RoleAssignment;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class RoleAssignmentTest {
	private final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
	private final Validator validator = factory.getValidator();
	
	private final UUID RESOURCE_ID = UUID.randomUUID();
	
	private final UUID IDENTITY_ID = UUID.randomUUID();

	@BeforeEach
	void setUp() throws Exception {
	}

	@Test
	void testNotNull() {
		RoleAssignment ra = RoleAssignment.builder().build();
		
		Set<ConstraintViolation<RoleAssignment>> violations = validator.validate(ra);
		assertThat(violations.size(), is(3));

	}
	
	@Test
	void testBuild() {
		RoleAssignment ra = RoleAssignment.builder().role(new Releaser()).identityId(IDENTITY_ID).resourceId(RESOURCE_ID).build();
		
		Set<ConstraintViolation<RoleAssignment>> violations = validator.validate(ra);
		assertThat(violations.size(), is(0));
		assertThat(ra.getResourceId(), is(RESOURCE_ID));

	}

}
