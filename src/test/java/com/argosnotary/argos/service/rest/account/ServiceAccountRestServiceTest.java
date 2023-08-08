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
package com.argosnotary.argos.service.rest.account;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;

import com.argosnotary.argos.domain.account.ServiceAccount;
import com.argosnotary.argos.domain.crypto.CryptoHelper;
import com.argosnotary.argos.domain.crypto.KeyPair;
import com.argosnotary.argos.service.account.AccountSecurityContext;
import com.argosnotary.argos.service.account.ServiceAccountService;
import com.argosnotary.argos.service.openapi.rest.model.RestKeyPair;
import com.argosnotary.argos.service.openapi.rest.model.RestServiceAccount;
import com.argosnotary.argos.service.openapi.rest.model.RestServiceAccountKeyPair;
import com.argosnotary.argos.service.openapi.rest.model.RestTokenRequest;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;

@ExtendWith(MockitoExtension.class)
class ServiceAccountRestServiceTest {
	
	private static final UUID accountId = UUID.fromString("9af61fef-a517-44fc-93a5-d5ae5fada255");
	private static final String passphrase = "wachtwoord";
	
	private ServiceAccountRestService serviceAccountRestService;
	
	private ServiceAccount sa1, sa1Updated, sa2;
	
	private RestServiceAccount rsa1;
	

	RestServiceAccountKeyPair rskp;
	RestKeyPair rkp;
	KeyPair kp;

    private ServiceAccountMapper accountMapper;
    
    private KeyPairMapper keyPairMapper;
    
    @Mock
    private ServiceAccountService serviceAccountService;
    
    @Mock
    private AccountSecurityContext accountSecurityContext;
	
	@Mock
    private HttpServletRequest httpServletRequest;
    
    private MockMvc mvc;

	@BeforeEach
	void setUp() throws Exception {
		accountMapper = Mappers.getMapper(ServiceAccountMapper.class);
		keyPairMapper = Mappers.getMapper(KeyPairMapper.class);
		serviceAccountRestService = new ServiceAccountRestServiceImpl(accountMapper, keyPairMapper, serviceAccountService, accountSecurityContext);
		kp = CryptoHelper.createKeyPair("test".toCharArray());
		rkp = keyPairMapper.convertToRestKeyPair(kp);
		rskp = new RestServiceAccountKeyPair(kp.getEncryptedPrivateKey(), "test", kp.getKeyId(), kp.getPublicKey());
		sa1 = ServiceAccount.builder().name("sa1").providerSubject("subject1").projectId(UUID.randomUUID()).build();
		sa2 = ServiceAccount.builder().name("sa2").providerSubject("subject2").activeKeyPair(kp).projectId(UUID.randomUUID()).build();
		rsa1 = accountMapper.convertToRestServiceAccount(sa1);
		sa1Updated = ServiceAccount.builder().name("sa1").providerSubject("subject1").projectId(sa1.getId()).activeKeyPair(kp).build();
	}

	@Test
	void testCreateServiceAccountKeyById() {
		ServletRequestAttributes servletRequestAttributes = new ServletRequestAttributes(httpServletRequest);
        RequestContextHolder.setRequestAttributes(servletRequestAttributes);
        
		when(serviceAccountService.findById(sa1.getId())).thenReturn(Optional.of(sa1));
		when(serviceAccountService.activateNewKey(sa1, keyPairMapper.convertFromRestServiceAccountKeyPair(rskp), "test".toCharArray())).thenReturn(sa1Updated);
		ResponseEntity<RestKeyPair> rkp = serviceAccountRestService.createServiceAccountKeyById(sa1.getProjectId(), sa1.getId(), rskp);
		KeyPair kp = keyPairMapper.convertFromRestServiceAccountKeyPair(rskp);
		assertEquals(kp, keyPairMapper.convertFromRestKeyPair(rkp.getBody()));
		
	}

	@Test
	void testCreateServiceAccountKeyByIdNoSa() {
		ServletRequestAttributes servletRequestAttributes = new ServletRequestAttributes(httpServletRequest);
        RequestContextHolder.setRequestAttributes(servletRequestAttributes);
        
		when(serviceAccountService.findById(sa1.getId())).thenReturn(Optional.empty());
		ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> serviceAccountRestService.createServiceAccountKeyById(sa1.getProjectId(), sa1.getId(), rskp));
        assertThat(exception.getMessage(), is(String.format("404 NOT_FOUND \"no service account with id : %s found\"", sa1.getId())));
		
	}

	@Test
	void testCreateServiceAccountKeyByIdNoPassphrase() {
		ServletRequestAttributes servletRequestAttributes = new ServletRequestAttributes(httpServletRequest);
        RequestContextHolder.setRequestAttributes(servletRequestAttributes);
        
        when(serviceAccountService.findById(sa1.getId())).thenReturn(Optional.of(sa1));
        rskp.setPassphrase(null);
		ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> serviceAccountRestService.createServiceAccountKeyById(sa1.getProjectId(), sa1.getId(), rskp));
        assertThat(exception.getMessage(), is(String.format("400 BAD_REQUEST \"Passphrase not available on request for sa [%s]\"", sa1.getName())));
		
	}

	@Test
    void createServiceAccount() {
        ServletRequestAttributes servletRequestAttributes = new ServletRequestAttributes(httpServletRequest);
        RequestContextHolder.setRequestAttributes(servletRequestAttributes);
        when(serviceAccountService.createServiceAccount(sa1)).thenReturn(sa1);
        ResponseEntity<RestServiceAccount> response = serviceAccountRestService.createServiceAccount(rsa1.getProjectId(), rsa1);
        assertThat(response.getStatusCode().value(), is(201));
        assertEquals(response.getBody(), rsa1);
        assertThat(response.getHeaders().getLocation(), notNullValue());
        verify(serviceAccountService).createServiceAccount(sa1);
    }

    @Test
    void createServiceAccountProjectIdNotEqual() {
        UUID id = UUID.randomUUID();
    	ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> serviceAccountRestService.createServiceAccount(id, rsa1));
        assertThat(exception.getMessage(), is(String.format("400 BAD_REQUEST \"projectIds not equal\"")));
    }

    @Test
    void createServiceAccountNameExistsOnProject() {
        ServletRequestAttributes servletRequestAttributes = new ServletRequestAttributes(httpServletRequest);
        RequestContextHolder.setRequestAttributes(servletRequestAttributes);
        when(serviceAccountService.exists(rsa1.getProjectId(), rsa1.getName())).thenReturn(true);
    	ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> serviceAccountRestService.createServiceAccount(rsa1.getProjectId(), rsa1));
        assertThat(exception.getMessage(), is(String.format("400 BAD_REQUEST \"Service account already exists with projectId [%s] and name [%s]\"", rsa1.getProjectId().toString(), rsa1.getName())));
    }

    @Test
    void getServiceAccountKeyById() {
        when(serviceAccountService.findById(sa2.getId())).thenReturn(Optional.of(sa2));
        ResponseEntity<RestKeyPair> response = serviceAccountRestService.getServiceAccountKeyById(sa2.getProjectId(), sa2.getId());
        assertThat(response.getStatusCode().value(), is(200));
        assertEquals(response.getBody(), rkp);
    }

    @Test
    void getServiceAccountKeyByIdAccountNotFound() {
    	when(serviceAccountService.findById(sa2.getId())).thenReturn(Optional.empty());
    	UUID id = sa2.getId();
    	UUID pId = sa2.getProjectId();
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> serviceAccountRestService.getServiceAccountKeyById(pId, id));
        assertThat(exception.getMessage(), is(String.format("404 NOT_FOUND \"no active service account key with id : %s found\"", sa2.getId().toString())));
    }

    @Test
    void getServiceAccountKeyByIdNoActiveKey() {
        when(serviceAccountService.findById(sa1.getId())).thenReturn(Optional.of(sa1));
    	UUID id = sa1.getId();
    	UUID pId = sa1.getProjectId();
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> serviceAccountRestService.getServiceAccountKeyById(pId, id));
        assertThat(exception.getMessage(), is(String.format("404 NOT_FOUND \"no active service account key with id : %s found\"", sa1.getId().toString())));
    }

    @Test
    void getServiceAccountById() {
        when(serviceAccountService.findById(sa1.getId())).thenReturn(Optional.of(sa1));
        ResponseEntity<RestServiceAccount> response = serviceAccountRestService.getServiceAccountById(sa1.getProjectId(), sa1.getId());
        assertThat(response.getStatusCode().value(), is(200));
        assertEquals(response.getBody(), rsa1);
    }

    @Test
    void getServiceAccountByIdAccountNotFound() {
        when(serviceAccountService.findById(sa1.getId())).thenReturn(Optional.empty());
    	UUID id = sa1.getId();
    	UUID pId = sa1.getProjectId();
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> serviceAccountRestService.getServiceAccountById(pId, id));
        assertThat(exception.getMessage(), is(String.format("404 NOT_FOUND \"no service account with id : %s found\"", sa1.getId().toString())));
    }

    @Test
    void deleteServiceAccount() {
    	when(serviceAccountService.findById(sa1.getId())).thenReturn(Optional.of(sa1));
        ResponseEntity<Void> response = serviceAccountRestService.deleteServiceAccount(sa1.getProjectId(), sa1.getId());
        assertThat(response.getStatusCode().value(), is(204));
        verify(serviceAccountService).delete(sa1);
    }

    @Test
    void deleteServiceAccountWithInvalidAccountIdShouldReturnNotFound() {
    	when(serviceAccountService.findById(sa1.getId())).thenReturn(Optional.empty());
    	UUID id = sa1.getId();
    	UUID pId = sa1.getProjectId();
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> serviceAccountRestService.deleteServiceAccount(pId, id));
        assertThat(exception.getStatusCode().value(), is(404));
        assertThat(exception.getMessage(), is(String.format("404 NOT_FOUND \"no service account with id : %s found\"", sa1.getId().toString())));
    }

    @Test
    void getServiceAccountKey() {
        when(accountSecurityContext.getAuthenticatedAccount()).thenReturn(Optional.of(sa2));
        ResponseEntity<RestKeyPair> response = serviceAccountRestService.getServiceAccountKey();
        assertThat(response.getStatusCode().value(), is(200));
        assertEquals(response.getBody(), rkp);
    }

    @Test
    void getServiceAccountKeyNotFound() {
        when(accountSecurityContext.getAuthenticatedAccount()).thenReturn(Optional.empty());
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> serviceAccountRestService.getServiceAccountKey());
        assertThat(exception.getMessage(), is("404 NOT_FOUND \"no active service account key found\""));
    }

    @Test
    void getServiceAccountKeyNoActiveKey() {
        when(accountSecurityContext.getAuthenticatedAccount()).thenReturn(Optional.of(sa1));
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> serviceAccountRestService.getServiceAccountKey());
        assertThat(exception.getMessage(), is("404 NOT_FOUND \"no active service account key found\""));
    }
    
    //@Test
    //@WithMockUser(username = "9af61fef-a517-44fc-93a5-d5ae5fada255", password = "wachtwoord")
    void getIdTokenAuth() throws Exception {
    	ObjectMapper mapper = new ObjectMapper(); // Mappers.getMapper(RestTokenRequest.class);
    	ServiceAccount sa = ServiceAccount.builder().id(accountId).build();
    	RestTokenRequest req = new RestTokenRequest(sa.getId(),passphrase);
    	this.mvc = MockMvcBuilders.standaloneSetup(serviceAccountRestService).build();
    	
    	when(serviceAccountService.getIdToken(sa, "wachtwoord".toCharArray())).thenReturn("token");
    	mvc.perform(get("/api/serviceaccounts/me/token")
              .accept("application/json")
              .content(mapper.writeValueAsBytes(req)))
    			.andExpect(status().isOk())
    			.andExpect(MockMvcResultMatchers.content().string(containsString("token")));
        
    }

}
