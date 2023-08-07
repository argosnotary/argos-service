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
import com.argosnotary.argos.service.nodes.ManagementNodeService;
import com.argosnotary.argos.service.nodes.NodeService;
import com.argosnotary.argos.service.openapi.rest.model.RestManagementNode;

import jakarta.servlet.http.HttpServletRequest;

@ExtendWith(MockitoExtension.class)
class ManagementNodeRestServiceTest {
	private static final String MN_NAME = "mnName";

	ManagementNodeRestService managementNodeRestService;
	
	private ManagementNodeMapper mapper = Mappers.getMapper(ManagementNodeMapper.class);

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private NodeService nodeService;
	
    @Mock
    ManagementNodeService managementNodeService;
    
    private MockMvc mvc;
    
    private Organization org;
    
    private ManagementNode node;
    
    private ManagementNode managementNode;
    
    @Mock
    private Project project;
    
    private RestManagementNode restManagementNode;

	@BeforeEach
	void setUp() throws Exception {
		managementNodeRestService = new ManagementNodeRestServiceImpl(managementNodeService, nodeService, mapper);
		
		org = new Organization(UUID.randomUUID(), "org", null);

        node = new ManagementNode(UUID.randomUUID(), "node", new ArrayList<>(), UUID.randomUUID());
        managementNode = new ManagementNode(UUID.randomUUID(), MN_NAME, new ArrayList<>(), org.getId());
        managementNode.setPathToRoot(List.of(org.getId(), org.getId()));
		restManagementNode = mapper.convertToRestManagementNode(managementNode);
	}
	
    @Test
    void whenUuidIsInvalid_thenReturnsStatus400() throws Exception {
    	this.mvc = MockMvcBuilders.standaloneSetup(managementNodeRestService).build();
    	String input = "invaliduuid";

    	mvc.perform(get("/api/managementnodes/"+input)
              .accept("application/json"))
    			.andExpect(status().isBadRequest());
    }
	
	@Test
	void testCreateManagementNode() throws URISyntaxException {
		UUID parentId = org.getId();
        when(nodeService.findById(parentId)).thenReturn(Optional.of(org));
        when(nodeService.existsByParentIdAndName(parentId, MN_NAME)).thenReturn(false);
        when(managementNodeService.create(managementNode)).thenReturn(managementNode);
        ServletRequestAttributes servletRequestAttributes = new ServletRequestAttributes(httpServletRequest);
        RequestContextHolder.setRequestAttributes(servletRequestAttributes);
        ResponseEntity<RestManagementNode> managementNodeItemResponse = managementNodeRestService.createManagementNode(parentId, restManagementNode);
        assertThat(managementNodeItemResponse.getStatusCode().value(), is(HttpStatus.CREATED.value()));
        assertEquals(managementNodeItemResponse.getHeaders().getLocation(), new URI(String.format("/api/managementnodes/%s", managementNode.getId())));
        assertThat(managementNodeItemResponse.getBody(), is(restManagementNode));
        verify(managementNodeService).create(managementNode);
	}
	

	
	@Test
	void testCreateManagementNodeWithNodeParent() {
		UUID parentId = node.getId();
		restManagementNode.setParentId(parentId);
		managementNode.setParentId(parentId);
        when(nodeService.findById(parentId)).thenReturn(Optional.of(node));
        when(nodeService.existsByParentIdAndName(parentId, MN_NAME)).thenReturn(false);
        when(managementNodeService.create(managementNode)).thenReturn(managementNode);
        ServletRequestAttributes servletRequestAttributes = new ServletRequestAttributes(httpServletRequest);
        RequestContextHolder.setRequestAttributes(servletRequestAttributes);
        ResponseEntity<RestManagementNode> managementNodeItemResponse = managementNodeRestService.createManagementNode(parentId, restManagementNode);
        assertThat(managementNodeItemResponse.getStatusCode().value(), is(HttpStatus.CREATED.value()));
        assertThat(managementNodeItemResponse.getHeaders().getLocation(), notNullValue());
        assertThat(managementNodeItemResponse.getBody(), is(restManagementNode));
        verify(managementNodeService).create(managementNode);
	}
	
	@Test
	void testCreateManagementNodeInvalidParentId() {
		UUID parentId = UUID.randomUUID();
        when(nodeService.findById(parentId)).thenReturn(Optional.of(org));
        ServletRequestAttributes servletRequestAttributes = new ServletRequestAttributes(httpServletRequest);
        RequestContextHolder.setRequestAttributes(servletRequestAttributes);
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
        	managementNodeRestService.createManagementNode(parentId, restManagementNode);
          });
        
        assertEquals("400 BAD_REQUEST \"invalid parent\"", exception.getMessage());
        assertThat(exception.getStatusCode().value(), is(400));
	}
	
	@Test
	void testCreateManagementNodeParentNotFound() {
		UUID parentId = org.getId();
        when(nodeService.findById(org.getId())).thenReturn(Optional.empty());
        ServletRequestAttributes servletRequestAttributes = new ServletRequestAttributes(httpServletRequest);
        RequestContextHolder.setRequestAttributes(servletRequestAttributes);
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
        	managementNodeRestService.createManagementNode(parentId, restManagementNode);
          });
        
        assertEquals("400 BAD_REQUEST \"invalid parent\"", exception.getMessage());
        assertThat(exception.getStatusCode().value(), is(400));
	}
	
	@Test
	void testCreateManagementNodeNodeIdWrong() {
		UUID parentId = org.getId();
		org.setId(UUID.randomUUID());
        when(nodeService.findById(parentId)).thenReturn(Optional.of(org));
        ServletRequestAttributes servletRequestAttributes = new ServletRequestAttributes(httpServletRequest);
        RequestContextHolder.setRequestAttributes(servletRequestAttributes);
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
        	managementNodeRestService.createManagementNode(parentId, restManagementNode);
          });
        
        assertEquals("400 BAD_REQUEST \"invalid parent\"", exception.getMessage());
        assertThat(exception.getStatusCode().value(), is(400));
	}
	
	@Test
	void testCreateManagementNodeWrongParentClass() {
		UUID parentId = UUID.randomUUID();
		when(project.getId()).thenReturn(parentId);
		restManagementNode.setParentId(parentId);
        when(nodeService.findById(parentId)).thenReturn(Optional.of(project));
        ServletRequestAttributes servletRequestAttributes = new ServletRequestAttributes(httpServletRequest);
        RequestContextHolder.setRequestAttributes(servletRequestAttributes);
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
        	managementNodeRestService.createManagementNode(parentId, restManagementNode);
          });
        
        assertEquals("400 BAD_REQUEST \"invalid parent\"", exception.getMessage());
        assertThat(exception.getStatusCode().value(), is(400));
	}
	

	
	@Test
	void testCreateManagementNodeNameUsed() {
		UUID parentId = org.getId();
        when(nodeService.findById(parentId)).thenReturn(Optional.of(org));
        when(nodeService.existsByParentIdAndName(parentId, MN_NAME)).thenReturn(true);
        ServletRequestAttributes servletRequestAttributes = new ServletRequestAttributes(httpServletRequest);
        RequestContextHolder.setRequestAttributes(servletRequestAttributes);
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
        	managementNodeRestService.createManagementNode(parentId, restManagementNode);
          });
        
        assertEquals(String.format("400 BAD_REQUEST \"Management Node with name [%s] already exists on parent [%s]\"", MN_NAME, parentId), exception.getMessage());
        assertThat(exception.getStatusCode().value(), is(400));
	}

	@Test
	void testDeleteManagementNodeById() {
		UUID id = managementNode.getId();
        when(managementNodeService.exists(id)).thenReturn(true);
        ServletRequestAttributes servletRequestAttributes = new ServletRequestAttributes(httpServletRequest);
        RequestContextHolder.setRequestAttributes(servletRequestAttributes);
        ResponseEntity<Void> managementNodeItemResponse = managementNodeRestService.deleteManagementNodeById(id);
        assertThat(managementNodeItemResponse.getStatusCode().value(), is(HttpStatus.NO_CONTENT.value()));
        verify(managementNodeService).delete(id);
	}

	@Test
	void testDeleteManagementNodeByIdNotFound() {
		UUID id = managementNode.getId();
        when(managementNodeService.exists(id)).thenReturn(false);
        ServletRequestAttributes servletRequestAttributes = new ServletRequestAttributes(httpServletRequest);
        RequestContextHolder.setRequestAttributes(servletRequestAttributes);
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
        	managementNodeRestService.deleteManagementNodeById(id);
        });
        
        assertEquals("404 NOT_FOUND \"ManagementNode not found\"", exception.getMessage());
        assertThat(exception.getStatusCode().value(), is(404));
	}

	@Test
	void testGetManagementNode() {
		UUID id = managementNode.getId();
        when(managementNodeService.findById(id)).thenReturn(Optional.of(managementNode));
        ServletRequestAttributes servletRequestAttributes = new ServletRequestAttributes(httpServletRequest);
        RequestContextHolder.setRequestAttributes(servletRequestAttributes);
        ResponseEntity<RestManagementNode> managementNodeItemResponse = managementNodeRestService.getManagementNode(id);
        assertThat(managementNodeItemResponse.getStatusCode().value(), is(HttpStatus.OK.value()));
        assertThat(managementNodeItemResponse.getBody(), is(restManagementNode));
	}

	@Test
	void testGetManagementNodeNotFound() {
		UUID id = managementNode.getId();
        when(managementNodeService.findById(id)).thenReturn(Optional.empty());
        ServletRequestAttributes servletRequestAttributes = new ServletRequestAttributes(httpServletRequest);
        RequestContextHolder.setRequestAttributes(servletRequestAttributes);
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
        	managementNodeRestService.getManagementNode(id);
        });
        assertEquals("404 NOT_FOUND \"ManagementNode not found\"", exception.getMessage());
        assertThat(exception.getStatusCode().value(), is(404));
	}

	@Test
	void testGetManagementNodes() {
		UUID parentId = managementNode.getId();
        when(nodeService.findById(parentId)).thenReturn(Optional.of(org));
        when(managementNodeService.find(Optional.of(org))).thenReturn(Set.of(managementNode));
        ServletRequestAttributes servletRequestAttributes = new ServletRequestAttributes(httpServletRequest);
        RequestContextHolder.setRequestAttributes(servletRequestAttributes);
        ResponseEntity<List<RestManagementNode>> managementNodeItemResponse = managementNodeRestService.getManagementNodes(parentId);
        assertThat(managementNodeItemResponse.getStatusCode().value(), is(HttpStatus.OK.value()));
        assertThat(managementNodeItemResponse.getBody(), is(List.of(restManagementNode)));
	}

	@Test
	void testGetManagementNodesParentIdNull() {
		when(managementNodeService.find(Optional.empty())).thenReturn(Set.of(managementNode));
        ServletRequestAttributes servletRequestAttributes = new ServletRequestAttributes(httpServletRequest);
        RequestContextHolder.setRequestAttributes(servletRequestAttributes);
        ResponseEntity<List<RestManagementNode>> managementNodeItemResponse = managementNodeRestService.getManagementNodes(null);
        assertThat(managementNodeItemResponse.getStatusCode().value(), is(HttpStatus.OK.value()));
        assertThat(managementNodeItemResponse.getBody(), is(List.of(restManagementNode)));
	}

	@Test
	void testGetManagementNodesParentNotFound() {
		UUID parentId = org.getId();
        when(nodeService.findById(parentId)).thenReturn(Optional.empty());
        ServletRequestAttributes servletRequestAttributes = new ServletRequestAttributes(httpServletRequest);
        RequestContextHolder.setRequestAttributes(servletRequestAttributes);
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
        	managementNodeRestService.getManagementNodes(parentId);
        });
        assertEquals(String.format("404 NOT_FOUND \"Node with id [%s] not found\"", parentId), exception.getMessage());
        assertThat(exception.getStatusCode().value(), is(404));
	}

}
