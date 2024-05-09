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
package com.argosnotary.argos.service.rest.nodes;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;

import com.argosnotary.argos.domain.account.PersonalAccount;
import com.argosnotary.argos.domain.account.ServiceAccount;
import com.argosnotary.argos.domain.nodes.Organization;
import com.argosnotary.argos.service.account.AccountSecurityContext;
import com.argosnotary.argos.service.nodes.NodeService;
import com.argosnotary.argos.service.nodes.OrganizationService;
import com.argosnotary.argos.service.openapi.rest.model.RestOrganization;

import jakarta.servlet.http.HttpServletRequest;

@ExtendWith(MockitoExtension.class)
class OrganizationRestServiceTest {
	
    private Organization org2;
    private RestOrganization restOrg;
	
	private OrganizationRestService organizationRestService;
	
	@Mock
	private OrganizationService organizationService;
	
	@Mock
	private NodeService nodeService;
	
	@Mock
	private AccountSecurityContext accountSecurityContext;
	
	private OrganizationMapper organizationMapper = Mappers.getMapper(OrganizationMapper.class);
	
	@Mock
    private HttpServletRequest httpServletRequest;
	
	private PersonalAccount pa;
	
	@Mock
	private ServiceAccount sa;

	@BeforeEach
	void setUp() throws Exception {
		organizationRestService = new OrganizationRestServiceImpl(organizationService,nodeService, organizationMapper,accountSecurityContext);
		organizationMapper = Mappers.getMapper(OrganizationMapper.class);
		
		org2 = new Organization(UUID.randomUUID(), "org2", null);
        
        pa = PersonalAccount.builder().name("pa").build();
        
        restOrg = organizationMapper.convertToRestOrganization(org2);
	}
	
	@Test
	void testCreateOrganization() throws URISyntaxException {
		ServletRequestAttributes servletRequestAttributes = new ServletRequestAttributes(httpServletRequest);
        RequestContextHolder.setRequestAttributes(servletRequestAttributes);
        when(accountSecurityContext.getAuthenticatedAccount()).thenReturn(Optional.of(pa));
        when(organizationService.existsByName(org2.getName())).thenReturn(false);
        when(organizationService.create(org2)).thenReturn(org2);
        ResponseEntity<RestOrganization> response = organizationRestService.createOrganization(restOrg);
        assertThat(response.getStatusCode(), is(HttpStatusCode.valueOf(201)));
        assertEquals(new URI(String.format("/api/organizations/%s", org2.getId())),response.getHeaders().getLocation());
        assertEquals(restOrg, response.getBody());
		
	}
	
	@Test
	void testCreateOrganizationNotAuthenticated() {
		ServletRequestAttributes servletRequestAttributes = new ServletRequestAttributes(httpServletRequest);
        RequestContextHolder.setRequestAttributes(servletRequestAttributes);
        when(accountSecurityContext.getAuthenticatedAccount()).thenReturn(Optional.empty());
        
        Throwable exception = assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> {
            organizationRestService.createOrganization(restOrg);
          });
        
        assertEquals("400 BAD_REQUEST \"invalid account\"", exception.getMessage());
		
	}
	
	@Test
	void testCreateOrganizationAuthenticatedServiceAccount() {
		ServletRequestAttributes servletRequestAttributes = new ServletRequestAttributes(httpServletRequest);
        RequestContextHolder.setRequestAttributes(servletRequestAttributes);
        when(accountSecurityContext.getAuthenticatedAccount()).thenReturn(Optional.of(sa));
        
        Throwable exception = assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> {
            organizationRestService.createOrganization(restOrg);
          });
        
        assertEquals("400 BAD_REQUEST \"invalid account\"", exception.getMessage());
		
	}
	
	@Test
	void testCreateOrganizationNameExists() throws URISyntaxException {
		ServletRequestAttributes servletRequestAttributes = new ServletRequestAttributes(httpServletRequest);
        RequestContextHolder.setRequestAttributes(servletRequestAttributes);
        when(accountSecurityContext.getAuthenticatedAccount()).thenReturn(Optional.of(pa));
        when(organizationService.existsByName(org2.getName())).thenReturn(true);
        Throwable exception = assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> {
            organizationRestService.createOrganization(restOrg);
          });
        
        assertEquals(String.format("400 BAD_REQUEST \"Organization with name [%s] already exists\"", org2.getName()), exception.getMessage());
		
	}

	@Test
	void testDeleteorganizationById() {

		ServletRequestAttributes servletRequestAttributes = new ServletRequestAttributes(httpServletRequest);
        RequestContextHolder.setRequestAttributes(servletRequestAttributes);
        when(organizationService.exists(org2.getId())).thenReturn(true);
        ResponseEntity<Void> response = organizationRestService.deleteOrganizationById(org2.getId());

        assertThat(response.getStatusCode(), is(HttpStatusCode.valueOf(204)));
		verify(organizationService).exists(org2.getId());
		verify(organizationService).delete(org2.getId());;
		
	}
	
	@Test
	void testDeleteOrganizationByIdNotFound() {
		ServletRequestAttributes servletRequestAttributes = new ServletRequestAttributes(httpServletRequest);
        RequestContextHolder.setRequestAttributes(servletRequestAttributes);
        when(organizationService.exists(org2.getId())).thenReturn(false);
        
        Throwable exception = assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> {
        	organizationRestService.deleteOrganizationById(org2.getId());
          });
        
        assertEquals("404 NOT_FOUND \"Organization not found\"", exception.getMessage());
		
	}

	@Test
	void testGetOrganization() {
		UUID id = org2.getId();
        when(organizationService.findById(id)).thenReturn(Optional.of(org2));
        ServletRequestAttributes servletRequestAttributes = new ServletRequestAttributes(httpServletRequest);
        RequestContextHolder.setRequestAttributes(servletRequestAttributes);
        ResponseEntity<RestOrganization> managementNodeItemResponse = organizationRestService.getOrganization(id);
        assertThat(managementNodeItemResponse.getStatusCode().value(), is(HttpStatus.OK.value()));
        assertThat(managementNodeItemResponse.getBody(), is(restOrg));
	}

	@Test
	void testGetOrganizationNotFound() {
		UUID id = org2.getId();
        when(organizationService.findById(id)).thenReturn(Optional.empty());
        ServletRequestAttributes servletRequestAttributes = new ServletRequestAttributes(httpServletRequest);
        RequestContextHolder.setRequestAttributes(servletRequestAttributes);
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
        	organizationRestService.getOrganization(id);
        });
        assertEquals("404 NOT_FOUND \"Organization not found\"", exception.getMessage());
        assertThat(exception.getStatusCode().value(), is(404));
	}

	@Test
	void testGetOrganizations() {
		UUID parentId = org2.getId();
        when(nodeService.findById(parentId)).thenReturn(Optional.of(org2));
        when(organizationService.find(Optional.of(org2))).thenReturn(Set.of(org2));
        ServletRequestAttributes servletRequestAttributes = new ServletRequestAttributes(httpServletRequest);
        RequestContextHolder.setRequestAttributes(servletRequestAttributes);
        ResponseEntity<List<RestOrganization>> managementNodeItemResponse = organizationRestService.getOrganizations(parentId);
        assertThat(managementNodeItemResponse.getStatusCode().value(), is(HttpStatus.OK.value()));
        assertThat(managementNodeItemResponse.getBody(), is(List.of(restOrg)));
	}

	@Test
	void testGetOrganizationsParentIdNull() {
		when(organizationService.find(Optional.empty())).thenReturn(Set.of(org2));
        ServletRequestAttributes servletRequestAttributes = new ServletRequestAttributes(httpServletRequest);
        RequestContextHolder.setRequestAttributes(servletRequestAttributes);
        ResponseEntity<List<RestOrganization>> managementNodeItemResponse = organizationRestService.getOrganizations(null);
        assertThat(managementNodeItemResponse.getStatusCode().value(), is(HttpStatus.OK.value()));
        assertThat(managementNodeItemResponse.getBody(), is(List.of(restOrg)));
	}

	@Test
	void testGetOrganizationsParentNotFound() {
		UUID parentId = org2.getId();
        when(nodeService.findById(parentId)).thenReturn(Optional.empty());
        ServletRequestAttributes servletRequestAttributes = new ServletRequestAttributes(httpServletRequest);
        RequestContextHolder.setRequestAttributes(servletRequestAttributes);
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
        	organizationRestService.getOrganizations(parentId);
        });
        assertEquals(String.format("404 NOT_FOUND \"Node with id [%s] not found\"", parentId), exception.getMessage());
        assertThat(exception.getStatusCode().value(), is(404));
	}

}
