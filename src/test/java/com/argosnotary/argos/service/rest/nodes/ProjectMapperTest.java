package com.argosnotary.argos.service.rest.nodes;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import com.argosnotary.argos.domain.nodes.Domain;
import com.argosnotary.argos.domain.nodes.ManagementNode;
import com.argosnotary.argos.domain.nodes.Organization;
import com.argosnotary.argos.domain.nodes.Project;
import com.argosnotary.argos.service.rest.nodes.ProjectMapper;

class ProjectMapperTest {
	
	private ProjectMapper projectMapper;
	
    private Organization org2;
    private ManagementNode node21, node22;
    private Project project211;

	@BeforeEach
	void setUp() throws Exception {
		projectMapper = Mappers.getMapper(ProjectMapper.class);
		
		org2 = new Organization(UUID.randomUUID(), "org2", null);
        
        org2.setDomain(Domain.builder().build());
        
        node21 = new ManagementNode(UUID.randomUUID(), "node21", org2);
        node22 = new ManagementNode(UUID.randomUUID(), "node22", org2);
        

        project211 = new Project(UUID.randomUUID(), "project211", node21);
	}

	@Test
	void testMapper() {
		Project node = projectMapper.convertFromRestProject(projectMapper.convertToRestProject(project211));
		assertThat(node.getChildren(), is(Set.of()));
		assertThat(node.getParentId(), is(node21.getId()));
	}

}
