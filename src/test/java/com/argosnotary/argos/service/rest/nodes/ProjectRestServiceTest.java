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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;

import com.argosnotary.argos.domain.nodes.ManagementNode;
import com.argosnotary.argos.domain.nodes.Organization;
import com.argosnotary.argos.domain.nodes.Project;
import com.argosnotary.argos.service.nodes.NodeService;
import com.argosnotary.argos.service.nodes.ProjectService;
import com.argosnotary.argos.service.openapi.rest.model.RestProject;

import jakarta.servlet.http.HttpServletRequest;

@ExtendWith(MockitoExtension.class)
class ProjectRestServiceTest {
	
	private static final UUID PARENT_ID = UUID.randomUUID();
	private static final UUID PROJECT_ID = UUID.randomUUID();
	private static final String PROJECT_NAME = "projectName";
	
	ProjectRestService projectRestService;
	
	private ProjectMapper mapper = Mappers.getMapper(ProjectMapper.class);

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private NodeService nodeService;
	
    @Mock
    ProjectService projectService;
    
    private MockMvc mvc;
    
    @Mock
    private Organization org;
    
    private ManagementNode node;
    
    private Project project;
    
    private RestProject restProject;

	@BeforeEach
	void setUp() throws Exception {
		projectRestService = new ProjectRestServiceImpl(projectService, nodeService, mapper);

        node = new ManagementNode(UUID.randomUUID(), "node", new ArrayList<>(), UUID.randomUUID());
		project = new Project(UUID.randomUUID(), PROJECT_NAME, new ArrayList<>(), node.getId());
		project.setPathToRoot(List.of(project.getId(), node.getId()));
		restProject = mapper.convertToRestProject(project);
	}
	
    @Test
    void whenUuidIsInvalid_thenReturnsStatus400() throws Exception {
    	this.mvc = MockMvcBuilders.standaloneSetup(projectRestService).build();
    	String input = "invaliduuid";

    	mvc.perform(get("/api/projects/"+input)
              .accept("application/json"))
    			.andExpect(status().isBadRequest());
    }
	
	@Test
	void testCreateProject() throws URISyntaxException {
        when(nodeService.findById(node.getId())).thenReturn(Optional.of(node));
        when(nodeService.existsByParentIdAndName(node.getId(), PROJECT_NAME)).thenReturn(false);
        when(projectService.create(project)).thenReturn(project);
        ServletRequestAttributes servletRequestAttributes = new ServletRequestAttributes(httpServletRequest);
        RequestContextHolder.setRequestAttributes(servletRequestAttributes);
        ResponseEntity<RestProject> projectItemResponse = projectRestService.createProject(node.getId(), restProject);
        assertThat(projectItemResponse.getStatusCode().value(), is(HttpStatus.CREATED.value()));
        assertEquals(projectItemResponse.getHeaders().getLocation(), new URI(String.format("/api/projects/%s", project.getId())));
        assertThat(projectItemResponse.getBody(), is(restProject));
        verify(projectService).create(project);
	}
	
	@Test
	void testCreateProjectWithOrgParent() {
		when(org.getId()).thenReturn(UUID.randomUUID());
		UUID parentId = org.getId();
		restProject.setParentId(parentId);
		project.setParentId(parentId);
        when(nodeService.findById(parentId)).thenReturn(Optional.of(org));
        when(nodeService.existsByParentIdAndName(parentId, PROJECT_NAME)).thenReturn(false);
        when(projectService.create(project)).thenReturn(project);
        ServletRequestAttributes servletRequestAttributes = new ServletRequestAttributes(httpServletRequest);
        RequestContextHolder.setRequestAttributes(servletRequestAttributes);
        ResponseEntity<RestProject> projectItemResponse = projectRestService.createProject(org.getId(), restProject);
        assertThat(projectItemResponse.getStatusCode().value(), is(HttpStatus.CREATED.value()));
        assertThat(projectItemResponse.getHeaders().getLocation(), notNullValue());
        assertThat(projectItemResponse.getBody(), is(restProject));
        verify(projectService).create(project);
	}
	
	@Test
	void testCreateProjectInvalidParentId() {
		UUID parentId = UUID.randomUUID();
        when(nodeService.findById(parentId)).thenReturn(Optional.of(node));
        ServletRequestAttributes servletRequestAttributes = new ServletRequestAttributes(httpServletRequest);
        RequestContextHolder.setRequestAttributes(servletRequestAttributes);
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
        	projectRestService.createProject(parentId, restProject);
          });
        
        assertEquals("400 BAD_REQUEST \"invalid parent\"", exception.getMessage());
        assertThat(exception.getStatusCode().value(), is(400));
	}
	
	@Test
	void testCreateProjectParentNotFound() {
		UUID parentId = node.getId();
        when(nodeService.findById(node.getId())).thenReturn(Optional.empty());
        ServletRequestAttributes servletRequestAttributes = new ServletRequestAttributes(httpServletRequest);
        RequestContextHolder.setRequestAttributes(servletRequestAttributes);
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
        	projectRestService.createProject(parentId, restProject);
          });
        
        assertEquals("400 BAD_REQUEST \"invalid parent\"", exception.getMessage());
        assertThat(exception.getStatusCode().value(), is(400));
	}
	
	@Test
	void testCreateProjectNodeIdWrong() {
		UUID parentId = node.getId();
		node.setId(UUID.randomUUID());
        when(nodeService.findById(parentId)).thenReturn(Optional.of(node));
        ServletRequestAttributes servletRequestAttributes = new ServletRequestAttributes(httpServletRequest);
        RequestContextHolder.setRequestAttributes(servletRequestAttributes);
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
        	projectRestService.createProject(parentId, restProject);
          });
        
        assertEquals("400 BAD_REQUEST \"invalid parent\"", exception.getMessage());
        assertThat(exception.getStatusCode().value(), is(400));
	}
	
	@Test
	void testCreateProjectWrongParentClass() {
		UUID parentId = project.getId();
		restProject.setParentId(parentId);
        when(nodeService.findById(parentId)).thenReturn(Optional.of(project));
        ServletRequestAttributes servletRequestAttributes = new ServletRequestAttributes(httpServletRequest);
        RequestContextHolder.setRequestAttributes(servletRequestAttributes);
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
        	projectRestService.createProject(parentId, restProject);
          });
        
        assertEquals("400 BAD_REQUEST \"invalid parent\"", exception.getMessage());
        assertThat(exception.getStatusCode().value(), is(400));
	}
	

	
	@Test
	void testCreateProjectNameUsed() {
		UUID parentId = node.getId();
        when(nodeService.findById(parentId)).thenReturn(Optional.of(node));
        when(nodeService.existsByParentIdAndName(parentId, PROJECT_NAME)).thenReturn(true);
        ServletRequestAttributes servletRequestAttributes = new ServletRequestAttributes(httpServletRequest);
        RequestContextHolder.setRequestAttributes(servletRequestAttributes);
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
        	projectRestService.createProject(parentId, restProject);
          });
        
        assertEquals(String.format("400 BAD_REQUEST \"Project with name [%s] already exists on parent [%s]\"", PROJECT_NAME, parentId), exception.getMessage());
        assertThat(exception.getStatusCode().value(), is(400));
	}

	@Test
	void testDeleteProjectById() {
		UUID id = project.getId();
        when(projectService.exists(id)).thenReturn(true);
        ServletRequestAttributes servletRequestAttributes = new ServletRequestAttributes(httpServletRequest);
        RequestContextHolder.setRequestAttributes(servletRequestAttributes);
        ResponseEntity<Void> projectItemResponse = projectRestService.deleteProjectById(id);
        assertThat(projectItemResponse.getStatusCode().value(), is(HttpStatus.NO_CONTENT.value()));
        verify(projectService).delete(id);
	}

	@Test
	void testDeleteProjectByIdNotFound() {
		UUID id = project.getId();
        when(projectService.exists(id)).thenReturn(false);
        ServletRequestAttributes servletRequestAttributes = new ServletRequestAttributes(httpServletRequest);
        RequestContextHolder.setRequestAttributes(servletRequestAttributes);
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
        	projectRestService.deleteProjectById(id);
        });
        
        assertEquals("404 NOT_FOUND \"Project not found\"", exception.getMessage());
        assertThat(exception.getStatusCode().value(), is(404));
	}

	@Test
	void testGetProject() {
		UUID id = project.getId();
        when(projectService.findById(id)).thenReturn(Optional.of(project));
        ServletRequestAttributes servletRequestAttributes = new ServletRequestAttributes(httpServletRequest);
        RequestContextHolder.setRequestAttributes(servletRequestAttributes);
        ResponseEntity<RestProject> projectItemResponse = projectRestService.getProject(id);
        assertThat(projectItemResponse.getStatusCode().value(), is(HttpStatus.OK.value()));
        assertThat(projectItemResponse.getBody(), is(restProject));
	}

	@Test
	void testGetProjectNotFound() {
		UUID id = project.getId();
        when(projectService.findById(id)).thenReturn(Optional.empty());
        ServletRequestAttributes servletRequestAttributes = new ServletRequestAttributes(httpServletRequest);
        RequestContextHolder.setRequestAttributes(servletRequestAttributes);
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
        	projectRestService.getProject(id);
        });
        assertEquals("404 NOT_FOUND \"Project not found\"", exception.getMessage());
        assertThat(exception.getStatusCode().value(), is(404));
	}

	@Test
	void testGetProjects() {
		UUID parentId = project.getId();
        when(nodeService.findById(parentId)).thenReturn(Optional.of(node));
        when(projectService.find(Optional.of(node))).thenReturn(Set.of(project));
        ServletRequestAttributes servletRequestAttributes = new ServletRequestAttributes(httpServletRequest);
        RequestContextHolder.setRequestAttributes(servletRequestAttributes);
        ResponseEntity<List<RestProject>> projectItemResponse = projectRestService.getProjects(parentId);
        assertThat(projectItemResponse.getStatusCode().value(), is(HttpStatus.OK.value()));
        assertThat(projectItemResponse.getBody(), is(List.of(restProject)));
	}

	@Test
	void testGetProjectsParentIdNull() {
		when(projectService.find(Optional.empty())).thenReturn(Set.of(project));
        ServletRequestAttributes servletRequestAttributes = new ServletRequestAttributes(httpServletRequest);
        RequestContextHolder.setRequestAttributes(servletRequestAttributes);
        ResponseEntity<List<RestProject>> projectItemResponse = projectRestService.getProjects(null);
        assertThat(projectItemResponse.getStatusCode().value(), is(HttpStatus.OK.value()));
        assertThat(projectItemResponse.getBody(), is(List.of(restProject)));
	}

	@Test
	void testGetProjectsParentNotFound() {
		UUID parentId = node.getId();
        when(nodeService.findById(parentId)).thenReturn(Optional.empty());
        ServletRequestAttributes servletRequestAttributes = new ServletRequestAttributes(httpServletRequest);
        RequestContextHolder.setRequestAttributes(servletRequestAttributes);
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
        	projectRestService.getProjects(parentId);
        });
        assertEquals(String.format("404 NOT_FOUND \"Node with id [%s] not found\"", parentId), exception.getMessage());
        assertThat(exception.getStatusCode().value(), is(404));
	}

}
