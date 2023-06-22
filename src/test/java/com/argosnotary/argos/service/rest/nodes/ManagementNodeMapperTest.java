package com.argosnotary.argos.service.rest.nodes;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import com.argosnotary.argos.domain.nodes.ManagementNode;
import com.argosnotary.argos.domain.nodes.Organization;
import com.argosnotary.argos.service.openapi.rest.model.RestManagementNode;
import com.argosnotary.argos.service.rest.nodes.ManagementNodeMapper;

class ManagementNodeMapperTest {
	
	private ManagementNodeMapper managementNodeMapper;
	
    private Organization org2;
    private ManagementNode node21, node211;

	@BeforeEach
	void setUp() throws Exception {
		managementNodeMapper = Mappers.getMapper(ManagementNodeMapper.class);
		
		org2 = new Organization(UUID.randomUUID(), "org2", null);
        
        node21 = new ManagementNode(UUID.randomUUID(), "node21", org2);
        node211 = new ManagementNode(UUID.randomUUID(), "node211", node21);
	}

	@Test
	void testMapper() {
		RestManagementNode restNode = managementNodeMapper.convertToRestManagementNode(node21);
		ManagementNode node = managementNodeMapper.convertFromRestManagementNode(restNode);
		assertThat(node.getChildren(), is(Set.of()));
		assertThat(node.getParentId(), is(org2.getId()));
	}

}
