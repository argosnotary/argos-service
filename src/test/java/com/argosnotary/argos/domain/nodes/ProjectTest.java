package com.argosnotary.argos.domain.nodes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.argosnotary.argos.domain.ArgosError;
import com.argosnotary.argos.domain.nodes.Node;
import com.argosnotary.argos.domain.nodes.Organization;
import com.argosnotary.argos.domain.nodes.Project;
import com.argosnotary.argos.domain.nodes.SupplyChain;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class ProjectTest {
	private Project proj1, proj2, proj3;
	private Organization org1, org2;
	private Set<Node> projectSet;
	
	private final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
	private final Validator validator = factory.getValidator();

	@BeforeEach
	void setUp() throws Exception {
		
	}

	@Test
	void testGetChildrenNoChildren() {
		Project proj = new Project();
		assertThat(proj.getChildren()).isEmpty();
	}
	
	@Test
	void testLeafNode() {
		Project proj = new Project();
		assertThat(proj.isLeaf()).isEqualTo(true);
		proj.setChildren(Set.of(new SupplyChain()));
		assertThat(proj.isLeaf()).isEqualTo(false);
		
	}
	
	@Test
	void testNameNotNull() {
		org1 = new Organization(UUID.randomUUID(), "org1", null);
		Project proj = new Project(UUID.randomUUID(), null, org1);
		Set<ConstraintViolation<Project>> violations = validator.validate(proj);
		assertThat(violations.size(), is(1));
	}
	
	@Test
	void testId() {
		UUID aId = UUID.randomUUID();
		Project proj = new Project();
		proj.setId(aId);
		assertThat(proj.getId()).isEqualTo(aId);
	}
	
	@Test
	void testParentAndId() {
		org1 = new Organization(UUID.randomUUID(), "org1", null);
		org2 = new Organization(UUID.randomUUID(), "org2", null);
		Project proj = new Project(UUID.randomUUID(), "proj", org1);
		Project proj2 = new Project(UUID.randomUUID(), "proj2", org1);
		assertThat(proj.getParent().get()).isEqualTo(org1);
		assertThat(proj.getParentId()).isEqualTo(org1.getId());
		proj.setParent(org2);
		assertThat(proj.getParent().get()).isEqualTo(org2);
		assertThat(proj.getParentId()).isEqualTo(org2.getId());
		

        Throwable exception = assertThrows(ArgosError.class, () -> {
        	proj.setParent(proj2);
          });
        
        assertEquals("Parent node of Project can only be a Organization or a ManagementNode but has class com.argosnotary.argos.domain.nodes.Project", exception.getMessage());
		
		
		
		
	}

}
