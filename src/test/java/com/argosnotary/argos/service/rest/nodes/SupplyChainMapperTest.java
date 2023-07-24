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
package com.argosnotary.argos.service.rest.nodes;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import com.argosnotary.argos.domain.nodes.ManagementNode;
import com.argosnotary.argos.domain.nodes.Organization;
import com.argosnotary.argos.domain.nodes.Project;
import com.argosnotary.argos.domain.nodes.SupplyChain;
import com.argosnotary.argos.service.rest.nodes.SupplyChainMapper;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class SupplyChainMapperTest {
	
	private SupplyChainMapper supplyChainMapper;
	
    private Project project11;
    private Organization org1;
    
    private SupplyChain sc;

    private final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
	private final Validator validator = factory.getValidator();

    @BeforeEach
    void setUp() {

        org1 = new Organization(UUID.randomUUID(), "org1", null);
        project11 = new Project(UUID.randomUUID(), "project11", org1);
        
		supplyChainMapper = Mappers.getMapper(SupplyChainMapper.class);
        sc = new SupplyChain(UUID.randomUUID(), "sc", project11);
        
	}

	@Test
	void testMapper() {
		SupplyChain node = supplyChainMapper.convertFromRestSupplyChain(supplyChainMapper.convertToRestSupplyChain(sc));
		assertThat(node.getChildren(), is(Set.of()));
		assertThat(node.getParentId(), is(project11.getId()));
	}

}
