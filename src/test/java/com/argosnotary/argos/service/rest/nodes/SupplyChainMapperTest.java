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
