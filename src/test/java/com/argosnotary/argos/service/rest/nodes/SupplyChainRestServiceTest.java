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

import static java.util.Optional.of;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.net.URI;
import java.net.URISyntaxException;
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

import com.argosnotary.argos.domain.nodes.Node;
import com.argosnotary.argos.domain.nodes.Project;
import com.argosnotary.argos.domain.nodes.SupplyChain;
import com.argosnotary.argos.service.nodes.NodeDeleteService;
import com.argosnotary.argos.service.nodes.NodeService;
import com.argosnotary.argos.service.nodes.SupplyChainService;
import com.argosnotary.argos.service.openapi.rest.model.RestSupplyChain;

import jakarta.servlet.http.HttpServletRequest;

@ExtendWith(MockitoExtension.class)
class SupplyChainRestServiceTest {

    private static final UUID PARENT_ID = UUID.randomUUID();
    private static final UUID SUPPLY_CHAIN_ID = UUID.randomUUID();
    private static final String SUPPLY_CHAIN_NAME = "supplyChainName";
    @Mock
    private SupplyChainService supplyChainService;
    
    private SupplyChainMapper converter = Mappers.getMapper(SupplyChainMapper.class);
    
    private Project project;
    
    private SupplyChain supplyChain;
    
    private RestSupplyChain restSupplyChain;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private NodeService nodeService;

    private SupplyChainRestService supplyChainRestService;

    @Mock
    private Node node;

    @Mock
    private NodeDeleteService deleteService;
    
    private MockMvc mvc;

    @BeforeEach
    public void setup() {
        supplyChainRestService = new SupplyChainRestServiceImpl(supplyChainService, nodeService, converter);
        supplyChain = new SupplyChain();
        supplyChain.setId(SUPPLY_CHAIN_ID);
        supplyChain.setName(SUPPLY_CHAIN_NAME);
        supplyChain.setParentId(PARENT_ID);
        restSupplyChain = converter.convertToRestSupplyChain(supplyChain);
        
        project = new Project();
        project.setId(PARENT_ID);
        
        
    }
    
    @Test
    void whenUuidIsInvalid_thenReturnsStatus400() throws Exception {
    	this.mvc = MockMvcBuilders.standaloneSetup(supplyChainRestService).build();
    	String input = "invaliduuid";

    	mvc.perform(get("/api/supplychains/"+input)
              .accept("application/json"))
    			.andExpect(status().isBadRequest());
    }

    @Test
    void createSupplyChain_With_UniqueName_Should_Return_201() throws URISyntaxException {
        when(nodeService.findById(PARENT_ID)).thenReturn(Optional.of(project));
        when(supplyChainService.create(supplyChain)).thenReturn(supplyChain);
        ServletRequestAttributes servletRequestAttributes = new ServletRequestAttributes(httpServletRequest);
        RequestContextHolder.setRequestAttributes(servletRequestAttributes);
        ResponseEntity<RestSupplyChain> supplyChainItemResponse = supplyChainRestService.createSupplyChain(PARENT_ID, restSupplyChain);
        assertThat(supplyChainItemResponse.getStatusCode().value(), is(HttpStatus.CREATED.value()));
        assertEquals(new URI(String.format("/api/supplychains/%s", supplyChain.getId())),supplyChainItemResponse.getHeaders().getLocation());
        assertThat(supplyChainItemResponse.getBody(), is(restSupplyChain));
        verify(supplyChainService).create((supplyChain));
    }
	
	@Test
	void testCreateSupplyChainInvalidParentId() {
		UUID parentId = UUID.randomUUID();
        when(nodeService.findById(parentId)).thenReturn(Optional.of(project));
        ServletRequestAttributes servletRequestAttributes = new ServletRequestAttributes(httpServletRequest);
        RequestContextHolder.setRequestAttributes(servletRequestAttributes);
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
        	supplyChainRestService.createSupplyChain(parentId, restSupplyChain);
          });
        
        assertEquals("400 BAD_REQUEST \"invalid parent\"", exception.getMessage());
        assertThat(exception.getStatusCode().value(), is(400));
	}

    @Test
    void createSupplyChain_With_Not_Existing_Parent_Should_Return_400() {
        when(nodeService.findById(PARENT_ID)).thenReturn(Optional.empty());
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> supplyChainRestService.createSupplyChain(PARENT_ID, restSupplyChain));
        assertThat(exception.getStatusCode(), is(HttpStatus.BAD_REQUEST));
        assertThat(exception.getMessage(), is("400 BAD_REQUEST \"invalid parent\""));
    }
	
	@Test
	void testCreateSupplyChainNodeIdWrong() {
		UUID parentId = project.getId();
		project.setId(UUID.randomUUID());
        when(nodeService.findById(parentId)).thenReturn(Optional.of(project));
        ServletRequestAttributes servletRequestAttributes = new ServletRequestAttributes(httpServletRequest);
        RequestContextHolder.setRequestAttributes(servletRequestAttributes);
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
        	supplyChainRestService.createSupplyChain(parentId, restSupplyChain);
          });
        
        assertEquals("400 BAD_REQUEST \"invalid parent\"", exception.getMessage());
        assertThat(exception.getStatusCode().value(), is(400));
	}
	
	@Test
	void testCreateSupplyChainWrongParentClass() {
		when(node.getId()).thenReturn(UUID.randomUUID());
		UUID parentId = node.getId();
		restSupplyChain.setParentId(parentId);
        when(nodeService.findById(parentId)).thenReturn(Optional.of(node));
        ServletRequestAttributes servletRequestAttributes = new ServletRequestAttributes(httpServletRequest);
        RequestContextHolder.setRequestAttributes(servletRequestAttributes);
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
        	supplyChainRestService.createSupplyChain(parentId, restSupplyChain);
          });
        
        assertEquals("400 BAD_REQUEST \"invalid parent\"", exception.getMessage());
        assertThat(exception.getStatusCode().value(), is(400));
	}
	

	
	@Test
	void testCreateProjectNameUsed() {
		UUID parentId = project.getId();
        when(nodeService.findById(parentId)).thenReturn(Optional.of(project));
        when(nodeService.existsByParentIdAndName(parentId, supplyChain.getName())).thenReturn(true);
        ServletRequestAttributes servletRequestAttributes = new ServletRequestAttributes(httpServletRequest);
        RequestContextHolder.setRequestAttributes(servletRequestAttributes);
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
        	supplyChainRestService.createSupplyChain(parentId, restSupplyChain);
          });
        
        assertEquals(String.format("400 BAD_REQUEST \"Supply Chain with name [%s] already exists on project [%s]\"", supplyChain.getName(), parentId), exception.getMessage());
        assertThat(exception.getStatusCode().value(), is(400));
	}


    @Test
    void getSupplyChain_With_Valid_Id_Should_Return_200() {
        when(supplyChainService.findById(SUPPLY_CHAIN_ID)).thenReturn(of(supplyChain));
        ResponseEntity<RestSupplyChain> supplyChainItemResponse = supplyChainRestService.getSupplyChain(SUPPLY_CHAIN_ID);
        assertThat(supplyChainItemResponse.getStatusCode().value(), is(HttpStatus.OK.value()));
        assertThat(supplyChainItemResponse.getBody(), is(restSupplyChain));
    }

    @Test
    void getSupplyChain_With_Valid_Id_Should_Return_404() {
        when(supplyChainService.findById(any())).thenReturn(Optional.empty());
        ResponseStatusException responseStatusException = assertThrows(ResponseStatusException.class, () ->
                supplyChainRestService.getSupplyChain(SUPPLY_CHAIN_ID));
        assertThat(responseStatusException.getStatusCode(), is(HttpStatus.NOT_FOUND));
    }

    @Test
    void updateSupplyChain() {
    	when(nodeService.exists(Project.class, PARENT_ID)).thenReturn(true);
        when(supplyChainService.findById(supplyChain.getId())).thenReturn(Optional.of(supplyChain));
        when(supplyChainService.update(supplyChain)).thenReturn(supplyChain);
        ResponseEntity<RestSupplyChain> response = supplyChainRestService.updateSupplyChain(supplyChain.getId(), restSupplyChain);
        assertThat(response.getStatusCode().value(), is(201));
        assertEquals(response.getBody(), restSupplyChain);
    }

    @Test
    void updateSupplyChainNotExits() {
    	when(nodeService.exists(Project.class, PARENT_ID)).thenReturn(true);
        when(supplyChainService.findById(supplyChain.getId())).thenReturn(Optional.empty());
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> supplyChainRestService.updateSupplyChain(SUPPLY_CHAIN_ID, restSupplyChain));
        assertThat(exception.getStatusCode().value(), is(404));
        assertThat(exception.getMessage(), is("404 NOT_FOUND \"SupplyChain not found\""));
    }
    
    @Test
    void updateRestSupplyChainIdNull() {
    	restSupplyChain.setId(null);
    	when(nodeService.exists(Project.class, PARENT_ID)).thenReturn(true);
        when(supplyChainService.findById(supplyChain.getId())).thenReturn(Optional.of(supplyChain));
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> supplyChainRestService.updateSupplyChain(SUPPLY_CHAIN_ID, restSupplyChain));
        assertThat(exception.getStatusCode().value(), is(400));
        assertThat(exception.getMessage(), is("400 BAD_REQUEST \"invalid supply chain\""));
    }
    
    @Test
    void updateIdsNotEqual() {
    	restSupplyChain.setId(UUID.randomUUID());
    	when(nodeService.exists(Project.class, PARENT_ID)).thenReturn(true);
        when(supplyChainService.findById(supplyChain.getId())).thenReturn(Optional.of(supplyChain));
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> supplyChainRestService.updateSupplyChain(SUPPLY_CHAIN_ID, restSupplyChain));
        assertThat(exception.getStatusCode().value(), is(400));
        assertThat(exception.getMessage(), is("400 BAD_REQUEST \"invalid supply chain\""));
    }
    
    @Test
    void updateWrgongType() {
    	restSupplyChain.setId(UUID.randomUUID());
    	when(nodeService.exists(Project.class, PARENT_ID)).thenReturn(true);
        when(supplyChainService.findById(supplyChain.getId())).thenReturn(Optional.of(supplyChain));
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> supplyChainRestService.updateSupplyChain(SUPPLY_CHAIN_ID, restSupplyChain));
        assertThat(exception.getStatusCode().value(), is(400));
        assertThat(exception.getMessage(), is("400 BAD_REQUEST \"invalid supply chain\""));
    }

    @Test
    void updateProjectNotExits() {
        when(nodeService.exists(Project.class, PARENT_ID)).thenReturn(false);
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> supplyChainRestService.updateSupplyChain(SUPPLY_CHAIN_ID, restSupplyChain));
        assertThat(exception.getStatusCode().value(), is(400));
        assertThat(exception.getMessage(), is("400 BAD_REQUEST \"Parent project not found\""));
    }

    @Test
    void deleteSupplyChainById() {
        when(supplyChainService.exists(SUPPLY_CHAIN_ID)).thenReturn(true);
        assertThat(supplyChainRestService.deleteSupplyChainById(SUPPLY_CHAIN_ID).getStatusCodeValue(), is(204));
        verify(supplyChainService).delete(SUPPLY_CHAIN_ID);
    }

    @Test
    void deleteSupplyChainByIdNotFound() {
        when(supplyChainService.exists(SUPPLY_CHAIN_ID)).thenReturn(false);
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> supplyChainRestService.deleteSupplyChainById(SUPPLY_CHAIN_ID));
        assertThat(exception.getStatusCode().value(), is(404));
        assertThat(exception.getMessage(), is(String.format("404 NOT_FOUND \"SupplyChain not found\"")));

    }

	@Test
	void testGetSupplyChains() {
		UUID parentId = project.getId();
        when(nodeService.findById(parentId)).thenReturn(Optional.of(project));
        when(supplyChainService.find(Optional.of(project))).thenReturn(Set.of(supplyChain));
        ServletRequestAttributes servletRequestAttributes = new ServletRequestAttributes(httpServletRequest);
        RequestContextHolder.setRequestAttributes(servletRequestAttributes);
        ResponseEntity<List<RestSupplyChain>> managementNodeItemResponse = supplyChainRestService.getSupplyChains(parentId);
        assertThat(managementNodeItemResponse.getStatusCode().value(), is(HttpStatus.OK.value()));
        assertThat(managementNodeItemResponse.getBody(), is(List.of(restSupplyChain)));
	}

	@Test
	void testGetSupplyChainsParentIdNull() {
		when(supplyChainService.find(Optional.empty())).thenReturn(Set.of(supplyChain));
        ServletRequestAttributes servletRequestAttributes = new ServletRequestAttributes(httpServletRequest);
        RequestContextHolder.setRequestAttributes(servletRequestAttributes);
        ResponseEntity<List<RestSupplyChain>> managementNodeItemResponse = supplyChainRestService.getSupplyChains(null);
        assertThat(managementNodeItemResponse.getStatusCode().value(), is(HttpStatus.OK.value()));
        assertThat(managementNodeItemResponse.getBody(), is(List.of(restSupplyChain)));
	}

	@Test
	void testGetSupplyChainsParentNotFound() {
		UUID parentId = project.getId();
        when(nodeService.findById(parentId)).thenReturn(Optional.empty());
        ServletRequestAttributes servletRequestAttributes = new ServletRequestAttributes(httpServletRequest);
        RequestContextHolder.setRequestAttributes(servletRequestAttributes);
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
        	supplyChainRestService.getSupplyChains(parentId);
        });
        assertEquals(String.format("404 NOT_FOUND \"Node with id [%s] not found\"", parentId), exception.getMessage());
        assertThat(exception.getStatusCode().value(), is(404));
	}
}
