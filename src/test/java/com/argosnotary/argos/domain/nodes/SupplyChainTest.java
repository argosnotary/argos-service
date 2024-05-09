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

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.argosnotary.argos.domain.ArgosError;
import com.argosnotary.argos.domain.account.ServiceAccount;

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
	void testNameNotNull() {
		SupplyChain sc = new SupplyChain(UUID.randomUUID(), null, List.of(), UUID.randomUUID());
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
		Project proj = new Project(UUID.randomUUID(), "proj", List.of(), UUID.randomUUID());
		SupplyChain sc = new SupplyChain(UUID.randomUUID(), "sc",List.of(), proj.getId());
		assertEquals(sc.getParentId(), proj.getId());
	}

}
