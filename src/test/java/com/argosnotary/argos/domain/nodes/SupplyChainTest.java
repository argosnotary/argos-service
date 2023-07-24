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
import com.argosnotary.argos.domain.account.ServiceAccount;
import com.argosnotary.argos.domain.nodes.Node;
import com.argosnotary.argos.domain.nodes.Organization;
import com.argosnotary.argos.domain.nodes.Project;
import com.argosnotary.argos.domain.nodes.SupplyChain;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class SupplyChainTest {
	private Project proj1, proj2, proj3;
	private SupplyChain supl1, supl2, supl3;
	private Organization org1, org2;
	private Set<Node> suplSet;
	private ServiceAccount sa;
	
	private final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
	private final Validator validator = factory.getValidator();

	@BeforeEach
	void setUp() throws Exception {
		
	}

	@Test
	void testGetChildrenNoChildren() {
		SupplyChain proj = new SupplyChain();
		assertThat(proj.getChildren()).isEmpty();
	}
	
	@Test
	void testLeafNode() {
		SupplyChain proj = new SupplyChain();
		assertThat(proj.isLeaf()).isEqualTo(true);
	}
	
	@Test
	void testNameNotNull() {
		org1 = new Organization(UUID.randomUUID(), "org1", null);
		Project proj = new Project(UUID.randomUUID(), "proj", org1);
		SupplyChain sc = new SupplyChain(UUID.randomUUID(), null, proj);
		Set<ConstraintViolation<SupplyChain>> violations = validator.validate(sc);
		assertThat(violations.size(), is(1));
	}
	
	@Test
	void testId() {
		UUID aId = UUID.randomUUID();
		SupplyChain sa = new SupplyChain();
		sa.setId(aId);
		assertThat(sa.getId()).isEqualTo(aId);
	}
	
	@Test
	void testGetParentId() {
		org1 = new Organization(UUID.randomUUID(), "org1", null);
		Project proj = new Project(UUID.randomUUID(), "proj", org1);
		SupplyChain sc = new SupplyChain(UUID.randomUUID(), "sc", proj);
		assertThat(sc.getParent().get()).isEqualTo(proj);
	}
	
	@Test
	void testGetParentAndId() {
		org1 = new Organization(UUID.randomUUID(), "org1", null);
		org2 = new Organization(UUID.randomUUID(), "org2", null);
		Project proj = new Project(UUID.randomUUID(), "proj", org1);
		assertThat(proj.getParent().get()).isEqualTo(org1);
		assertThat(proj.getParentId()).isEqualTo(org1.getId());
		proj.setParent(org2);
		assertThat(proj.getParent().get()).isEqualTo(org2);
		assertThat(proj.getParentId()).isEqualTo(org2.getId());
	}
	

	
	@Test
	void testParentAndId() {
		org1 = new Organization(UUID.randomUUID(), "org1", null);
		Project proj = new Project(UUID.randomUUID(), "proj", org1);
		SupplyChain sc = new SupplyChain(UUID.randomUUID(), "sc", proj);
		assertThat(sc.getParent().get()).isEqualTo(proj);
		assertThat(sc.getParentId()).isEqualTo(proj.getId());
		Project proj2 = new Project(UUID.randomUUID(), "proj2", org1);
		sc.setParent(proj2);
		assertThat(sc.getParent().get()).isEqualTo(proj2);
		assertThat(sc.getParentId()).isEqualTo(proj2.getId());
		

        Throwable exception = assertThrows(ArgosError.class, () -> {
        	sc.setParent(org1);
          });
        
        assertEquals("Parent node of SupplyChain can only be a Project but was class com.argosnotary.argos.domain.nodes.Organization", exception.getMessage());
		
		
		
		
	}

}
