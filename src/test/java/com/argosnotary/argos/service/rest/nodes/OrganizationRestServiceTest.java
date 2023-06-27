package com.argosnotary.argos.service.rest.nodes;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.argosnotary.argos.domain.account.PersonalAccount;
import com.argosnotary.argos.domain.nodes.ManagementNode;
import com.argosnotary.argos.domain.nodes.Organization;
import com.argosnotary.argos.service.account.AccountSecurityContext;
import com.argosnotary.argos.service.nodes.NodeService;
import com.argosnotary.argos.service.nodes.OrganizationService;
import com.argosnotary.argos.service.openapi.rest.model.RestOrganization;
import com.argosnotary.argos.service.rest.nodes.OrganizationMapper;
import com.argosnotary.argos.service.rest.nodes.OrganizationRestService;
import com.argosnotary.argos.service.rest.nodes.OrganizationRestServiceImpl;

import jakarta.servlet.http.HttpServletRequest;

@ExtendWith(MockitoExtension.class)
class OrganizationRestServiceTest {
	
    private Organization org2;
    private RestOrganization restOrg;
    private ManagementNode node21, node22;
	
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

	@BeforeEach
	void setUp() throws Exception {
		organizationRestService = new OrganizationRestServiceImpl(organizationService,organizationMapper,accountSecurityContext);
		organizationMapper = Mappers.getMapper(OrganizationMapper.class);
		
		org2 = new Organization(UUID.randomUUID(), "org2", null);
        
        pa = PersonalAccount.builder().name("pa").build();
        
        node21 = new ManagementNode(UUID.randomUUID(), "node21", org2);
        node22 = new ManagementNode(UUID.randomUUID(), "node22", org2);
        restOrg = organizationMapper.convertToRestOrganization(org2);
	}
	
	@Test
	void testCreateOrganizationBadReq() {
		ServletRequestAttributes servletRequestAttributes = new ServletRequestAttributes(httpServletRequest);
        RequestContextHolder.setRequestAttributes(servletRequestAttributes);
        
        Throwable exception = assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> {
            organizationRestService.createOrganization(restOrg);
          });
        
        assertEquals("400 BAD_REQUEST \"invalid account\"", exception.getMessage());
		
	}
	
	@Test
	void testCreateOrganization() {
		ServletRequestAttributes servletRequestAttributes = new ServletRequestAttributes(httpServletRequest);
        RequestContextHolder.setRequestAttributes(servletRequestAttributes);
        when(accountSecurityContext.getAuthenticatedAccount()).thenReturn(Optional.of(pa));
        when(organizationService.create(org2)).thenReturn(org2);
        ResponseEntity<RestOrganization> response = organizationRestService.createOrganization(restOrg);
        assertThat(response.getStatusCode(), is(HttpStatusCode.valueOf(201)));
        assertEquals(response.getBody(), restOrg);
        assertThat(response.getHeaders().getLocation(), notNullValue());
		verify(accountSecurityContext).getAuthenticatedAccount();
		
	}

	@Test
	void testDeleteorganizationById() {

		ServletRequestAttributes servletRequestAttributes = new ServletRequestAttributes(httpServletRequest);
        RequestContextHolder.setRequestAttributes(servletRequestAttributes);
        when(organizationService.existsById(org2.getId())).thenReturn(true);
        ResponseEntity<Void> response = organizationRestService.deleteOrganizationById(org2.getId());

        assertThat(response.getStatusCode(), is(HttpStatusCode.valueOf(204)));
		verify(organizationService).existsById(org2.getId());
		verify(organizationService).delete(org2.getId());;
		
	}
	
	@Test
	void testDeleteOrganizationByIdNotFound() {
		ServletRequestAttributes servletRequestAttributes = new ServletRequestAttributes(httpServletRequest);
        RequestContextHolder.setRequestAttributes(servletRequestAttributes);
        when(organizationService.existsById(org2.getId())).thenReturn(false);
        
        Throwable exception = assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> {
        	organizationRestService.deleteOrganizationById(org2.getId());
          });
        
        assertEquals("404 NOT_FOUND \"Organization not found\"", exception.getMessage());
		
	}

	@Test
	void testGetOrganization() {
		
	}

	@Test
	void testGetOrganizations() {
		
	}

}
