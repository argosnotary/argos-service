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
package com.argosnotary.argos.domain.nodes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.argosnotary.argos.domain.ArgosError;

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
	void testNameNotNull() {
		org1 = new Organization(UUID.randomUUID(), "org1", null);
		Project proj = new Project(UUID.randomUUID(), null, new ArrayList<>(), org1.getId());
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
		Project proj = new Project(UUID.randomUUID(), "proj", new ArrayList<>(), org1.getId());
		Project proj2 = new Project(UUID.randomUUID(), "proj2", new ArrayList<>(), org1.getId());
		assertThat(proj.getParentId()).isEqualTo(org1.getId());
		
	}

}
