package com.argosnotary.argos.service.rest.nodes;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNull;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import com.argosnotary.argos.domain.nodes.Domain;
import com.argosnotary.argos.domain.nodes.ManagementNode;
import com.argosnotary.argos.domain.nodes.Organization;
import com.argosnotary.argos.service.openapi.rest.model.RestOrganization;
import com.argosnotary.argos.service.rest.nodes.OrganizationMapper;

class OrganizationMapperTest {
	
	private OrganizationMapper organizationMapper;
	
    private Organization org2;
    private ManagementNode node21, node22;

	@BeforeEach
	void setUp() throws Exception {
		organizationMapper = Mappers.getMapper(OrganizationMapper.class);
		
		org2 = new Organization(UUID.randomUUID(), "org2", null);
        
        org2.setDomain(Domain.builder().build());
        
        node21 = new ManagementNode(UUID.randomUUID(), "node21", org2);
        node22 = new ManagementNode(UUID.randomUUID(), "node22", org2);
	}

	@Test
	void testMapper() {
		RestOrganization restOrg = organizationMapper.convertToRestOrganization(org2);
		Organization org = organizationMapper.convertFromRestOrganization(restOrg);
		assertThat(org.getChildren(), is(Set.of()));
		assertNull(org.getDomain());
		assertThat(org.getParent(), is(Optional.empty()));
	}

}
