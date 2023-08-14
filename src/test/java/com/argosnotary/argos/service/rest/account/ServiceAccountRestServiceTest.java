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
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;

import com.argosnotary.argos.domain.account.ServiceAccount;
import com.argosnotary.argos.domain.crypto.CryptoHelper;
import com.argosnotary.argos.domain.crypto.KeyPair;
import com.argosnotary.argos.service.account.AccountSecurityContext;
import com.argosnotary.argos.service.account.ServiceAccountService;
import com.argosnotary.argos.service.openapi.rest.model.RestJwtToken;
import com.argosnotary.argos.service.openapi.rest.model.RestKeyPair;
import com.argosnotary.argos.service.openapi.rest.model.RestServiceAccount;
import com.argosnotary.argos.service.openapi.rest.model.RestServiceAccountKeyPair;
import com.argosnotary.argos.service.openapi.rest.model.RestTokenRequest;
import com.argosnotary.argos.service.rest.KeyPairMapper;
import com.argosnotary.argos.service.rest.KeyPairMapperImpl;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.NotAuthorizedException;

@SpringBootTest(classes= {ServiceAccountMapperImpl.class, KeyPairMapperImpl.class})
class ServiceAccountRestServiceTest {
	
	private static final UUID accountId = UUID.fromString("9af61fef-a517-44fc-93a5-d5ae5fada255");
	private static final String passphrase = "wachtwoord";
	
	private ServiceAccountRestService serviceAccountRestService;
	
	private ServiceAccount sa1, sa1Updated, sa2;
	
	private RestServiceAccount rsa1;
	

	RestServiceAccountKeyPair rskp;
	RestKeyPair rkp;
	KeyPair kp;

    @Autowired
	private ServiceAccountMapper accountMapper;
    
    @Autowired
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
		serviceAccountRestService = new ServiceAccountRestServiceImpl(accountMapper, keyPairMapper, serviceAccountService, accountSecurityContext);
		kp = CryptoHelper.createKeyPair("test".toCharArray());
		rkp = keyPairMapper.convertToRestKeyPair(kp);
		rskp = new RestServiceAccountKeyPair(kp.getEncryptedPrivateKey(), "test", kp.getKeyId(), kp.getPub());
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
	void testCreateServiceAccountKeyByIdWrongProjectId() {
		ServletRequestAttributes servletRequestAttributes = new ServletRequestAttributes(httpServletRequest);
        RequestContextHolder.setRequestAttributes(servletRequestAttributes);
        
        UUID saId = sa1.getId();
        UUID randomId = UUID.randomUUID();
        
		when(serviceAccountService.findById(sa1.getId())).thenReturn(Optional.of(sa1));
		when(serviceAccountService.activateNewKey(sa1, keyPairMapper.convertFromRestServiceAccountKeyPair(rskp), "test".toCharArray())).thenReturn(sa1Updated);
		ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->  {
			serviceAccountRestService.createServiceAccountKeyById(randomId, saId, rskp);
		});
		assertThat(exception.getMessage(), is("400 BAD_REQUEST \"projectIds not equal\""));
		
	}

	@Test
	void testCreateServiceAccountKeyByIdNoSa() {
    	UUID sa1ProjectId = sa1.getProjectId();
    	UUID sa1Id = sa1.getId();
		ServletRequestAttributes servletRequestAttributes = new ServletRequestAttributes(httpServletRequest);
        RequestContextHolder.setRequestAttributes(servletRequestAttributes);
        
		when(serviceAccountService.findById(sa1.getId())).thenReturn(Optional.empty());
		ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> serviceAccountRestService.createServiceAccountKeyById(sa1ProjectId, sa1Id, rskp));
        assertThat(exception.getMessage(), is(String.format("404 NOT_FOUND \"no service account with id : %s found\"", sa1.getId())));
		
	}

	@Test
	void testCreateServiceAccountKeyByIdNoPassphrase() {
    	UUID sa1ProjectId = sa1.getProjectId();
    	UUID sa1Id = sa1.getId();
		ServletRequestAttributes servletRequestAttributes = new ServletRequestAttributes(httpServletRequest);
        RequestContextHolder.setRequestAttributes(servletRequestAttributes);
        
        when(serviceAccountService.findById(sa1.getId())).thenReturn(Optional.of(sa1));
        rskp.setPassphrase(null);
		ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> serviceAccountRestService.createServiceAccountKeyById(sa1ProjectId, sa1Id, rskp));
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
    	UUID rsa1ProjectId = rsa1.getProjectId();
    	ServletRequestAttributes servletRequestAttributes = new ServletRequestAttributes(httpServletRequest);
        RequestContextHolder.setRequestAttributes(servletRequestAttributes);
        when(serviceAccountService.exists(rsa1.getProjectId(), rsa1.getName())).thenReturn(true);
    	ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> serviceAccountRestService.createServiceAccount(rsa1ProjectId, rsa1));
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
    void getServiceAccountKeyByIdWrongProjectId() {
        when(serviceAccountService.findById(sa1.getId())).thenReturn(Optional.of(sa1));
    	UUID id = sa1.getId();
    	UUID pId = UUID.randomUUID();
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> serviceAccountRestService.getServiceAccountKeyById(pId, id));
        assertThat(exception.getStatusCode().value(), is(400));
        assertThat(exception.getMessage(), is("400 BAD_REQUEST \"projectIds not equal\""));
    }

    @Test
    void getServiceAccountById() {
        when(serviceAccountService.findById(sa1.getId())).thenReturn(Optional.of(sa1));
        ResponseEntity<RestServiceAccount> response = serviceAccountRestService.getServiceAccountById(sa1.getProjectId(), sa1.getId());
        assertThat(response.getStatusCode().value(), is(200));
        assertEquals(response.getBody(), rsa1);
    }

    @Test
    void getServiceAccountByIdWrongProjectId() {
        when(serviceAccountService.findById(sa1.getId())).thenReturn(Optional.of(sa1));
        UUID id = sa1.getId();
        UUID rId = UUID.randomUUID();
    	ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> serviceAccountRestService.getServiceAccountById(rId, id));
        assertThat(exception.getStatusCode().value(), is(400));
        assertThat(exception.getMessage(), is("400 BAD_REQUEST \"projectIds not equal\""));
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
    void deleteServiceAccountWrongprojectId() {
    	UUID sa1Id = sa1.getId();
    	UUID rId = UUID.randomUUID();
    	when(serviceAccountService.findById(sa1.getId())).thenReturn(Optional.of(sa1));
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> 
        	serviceAccountRestService.deleteServiceAccount(rId, sa1Id));
        assertThat(exception.getMessage(), is("400 BAD_REQUEST \"projectIds not equal\""));
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
    
    @Test
    void getIdTokenAuth() throws Exception {
    	RestTokenRequest restTokenRequest = new RestTokenRequest(accountId, passphrase);
    	RestJwtToken jwtToken = new RestJwtToken("jwtToken");
    	ServiceAccount sa = ServiceAccount.builder().id(accountId).build();
    	when(serviceAccountService.getIdToken(sa, passphrase.toCharArray())).thenReturn(jwtToken.getToken());
    	
    	ResponseEntity<RestJwtToken> response = serviceAccountRestService.getIdToken(restTokenRequest);
        assertThat(response.getStatusCode().value(), is(200));
        assertThat(response.getBody(), is(jwtToken));
        
    }
    
    @Test
    void getIdTokenNotAuth() throws Exception {
    	RestTokenRequest restTokenRequest = new RestTokenRequest(accountId, passphrase);
    	RestJwtToken jwtToken = new RestJwtToken("jwtToken");
    	ServiceAccount sa = ServiceAccount.builder().id(accountId).build();
    	when(serviceAccountService.getIdToken(sa, passphrase.toCharArray())).thenThrow(new NotAuthorizedException("not authorized"));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> serviceAccountRestService.getIdToken(restTokenRequest));
        assertThat(exception.getMessage(), is(String.format("401 UNAUTHORIZED \"Service Account [%s] is not authorized\"", sa.getId().toString())));
        assertThat(exception.getStatusCode().value(), is(401));
        
    }

}
