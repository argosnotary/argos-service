/*
 * Argos Notary - A new way to secure the Software Supply Chain
 *
 * Copyright (C) 2019 - 2020 Rabobank Nederland
 * Copyright (C) 2019 - 2025 Gerard Borst <gerard.borst@argosnotary.com>
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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import com.argosnotary.argos.domain.nodes.Domain;
import com.argosnotary.argos.domain.nodes.ManagementNode;
import com.argosnotary.argos.domain.nodes.Organization;
import com.argosnotary.argos.domain.nodes.Project;
import com.argosnotary.argos.domain.nodes.SupplyChain;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class SupplyChainMapperTest {
	
	private SupplyChainMapper supplyChainMapper;
	
    private Project project211;
    private Organization org2;
    private ManagementNode node21;
    
    private SupplyChain sc;

    private final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
	private final Validator validator = factory.getValidator();

    @BeforeEach
    void setUp() {

        supplyChainMapper = Mappers.getMapper(SupplyChainMapper.class);
        
		org2 = new Organization(UUID.randomUUID(), "org2", null);
        
        org2.setDomain(Domain.builder().build());
        
        node21 = new ManagementNode(UUID.randomUUID(), "node21", List.of(), org2.getId());
        
        node21.setPathToRoot(List.of(node21.getId(), org2.getId()));
        

        project211 = new Project(UUID.randomUUID(), "project211",List.of(),  node21.getId());
        
        project211.setPathToRoot(List.of(project211.getId(), node21.getId(), org2.getId()));
        
        sc = new SupplyChain(UUID.randomUUID(), "sc", List.of(), project211.getId());
        sc.setPathToRoot(List.of(sc.getId(), project211.getId(), node21.getId(), org2.getId()));
		
        
	}

	@Test
	void testMapper() {
		SupplyChain node = supplyChainMapper.convertFromRestSupplyChain(supplyChainMapper.convertToRestSupplyChain(sc));

		assertEquals(sc, node);
		
	}

}
