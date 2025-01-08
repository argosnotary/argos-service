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
package com.argosnotary.argos.service.account;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.argosnotary.argos.domain.ArgosError;
import com.argosnotary.argos.domain.account.ServiceAccount;
import com.argosnotary.argos.service.ArgosTestContainers;

import jakarta.ws.rs.NotAuthorizedException;

@Testcontainers
@ExtendWith(MockitoExtension.class)
class ServiceAccountProviderServiceTest {
	
	private static String getKeycloakUrl(String realm) {
		String url = String.format("http://%s:%s", ///realms/%s", 
				keycloakContainer.getHost(), 
				keycloakContainer.getFirstMappedPort(),
				realm);
		return url;
	}
	
	@Container //
	private static GenericContainer keycloakContainer = ArgosTestContainers.getKeycloakContainer();

	
	@Autowired
	ServiceAccountProviderService serviceAccountProviderService;
	
	ServiceAccount sa;
	
	String providerUrl;
	
	@Mock
	ClientRegistrationService clientRegistrationService;
	
	@Mock
	UserRepresentation keycloakUser;
	
	@DynamicPropertySource
	static void setProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.security.oauth2.client.provider.saprovider.issuer-uri", () -> "issuer-uri");
	}
	
	@BeforeEach
	void setUp() throws Exception {
		providerUrl = getKeycloakUrl("saprovider");
		when(clientRegistrationService.exists(ServiceAccount.SA_PROVIDER_NAME)).thenReturn(true);
		when(clientRegistrationService.getClientRegistrationProviderUrl(ServiceAccount.SA_PROVIDER_NAME)).thenReturn(Optional.of(providerUrl));
		
		serviceAccountProviderService = new ServiceAccountProviderServiceImpl(
				clientRegistrationService, 
				"samanager", "0weeMaHVQ3GqriognQg8QkEwBmoVC7LB",
				"saprovider-client", "644TyDbo7pTeyqDLM7kj4LWMwjRcUcBr");
		sa = ServiceAccount.builder().id(UUID.randomUUID()).name("sa").projectId(UUID.randomUUID()).build();
	}
	
	@Test
	void testConstructor() {
		when(clientRegistrationService.exists(ServiceAccount.SA_PROVIDER_NAME)).thenReturn(false);
		Throwable exception = assertThrows(ArgosError.class, () -> {
			new ServiceAccountProviderServiceImpl(
					clientRegistrationService, 
					"samanager", "0weeMaHVQ3GqriognQg8QkEwBmoVC7LB",
					"saprovider-client", "644TyDbo7pTeyqDLM7kj4LWMwjRcUcBr");
        });
		assertEquals("OIDC provider definition of [saprovider] doesn't exist", exception.getMessage());
		
	}

	@Test
	void testRegisterServiceAccount() {
		ServiceAccount sa2 = serviceAccountProviderService.registerServiceAccount(sa);
		assertEquals(sa.getId(), sa2.getId());
		assertNotNull(sa2.getProviderSubject());
		
		assertTrue(serviceAccountProviderService.exists(sa2.getId()));
	}
	
	@Test
	void testRegisterServiceAccountStatusNotOk() {
		// no samanager client
		ServiceAccountProviderService service = new ServiceAccountProviderServiceImpl(
				clientRegistrationService,
				"saprovider-client", "644TyDbo7pTeyqDLM7kj4LWMwjRcUcBr",
				"saprovider-client", "644TyDbo7pTeyqDLM7kj4LWMwjRcUcBr");
		Throwable exception = assertThrows(ArgosError.class, () -> {
			service.registerServiceAccount(sa);
        });
		assertEquals("Error in registering a service account message: [jakarta.ws.rs.NotAuthorizedException: HTTP 401 Unauthorized]", exception.getMessage());
	}
	
	@Test
	void testExists() {
		assertFalse(serviceAccountProviderService.exists(sa.getId()));
		ServiceAccount sa2 = serviceAccountProviderService.registerServiceAccount(sa);
		assertTrue(serviceAccountProviderService.exists(sa.getId()));
	}
	
	@Test
	void testUnRegisterServiceAccount() {
		ServiceAccount sa2 = serviceAccountProviderService.registerServiceAccount(sa);
		assertEquals(sa.getId(), sa2.getId());
		assertNotNull(sa2.getProviderSubject());
		serviceAccountProviderService.unRegisterServiceAccount(sa2);
		
		assertFalse(serviceAccountProviderService.exists(sa2.getId()));

		serviceAccountProviderService.unRegisterServiceAccount(sa2);
	}
	
	@Test
	void testUnRegisterServiceAccountStatusNotOk() {
		ServiceAccount sa2 = serviceAccountProviderService.registerServiceAccount(sa);
		// no samanager client
		ServiceAccountProviderService service = new ServiceAccountProviderServiceImpl(
				clientRegistrationService,
				"saprovider-client", "644TyDbo7pTeyqDLM7kj4LWMwjRcUcBr",
				"saprovider-client", "644TyDbo7pTeyqDLM7kj4LWMwjRcUcBr");
		Throwable exception = assertThrows(ArgosError.class, () -> {
			service.unRegisterServiceAccount(sa2);
        });
		assertEquals(String.format("Service Account unregister with id: [%s] failed with message [jakarta.ws.rs.NotAuthorizedException: HTTP 401 Unauthorized]", sa2.getId()), exception.getMessage());
	}
	
	@Test
	void testSetServiceAccountPassword() {
		ServiceAccount sa2 = serviceAccountProviderService.registerServiceAccount(sa);
		assertEquals(sa.getId(), sa2.getId());
		assertNotNull(sa2.getProviderSubject());
		serviceAccountProviderService.setServiceAccountPassword(sa2, "password1".toCharArray());
		String token = serviceAccountProviderService.getIdToken(sa2.getId(), "password1".toCharArray());
		assertNotNull(token);
		
		serviceAccountProviderService.setServiceAccountPassword(sa2, "password2".toCharArray());
		token = serviceAccountProviderService.getIdToken(sa2.getId(), "password2".toCharArray());
		assertNotNull(token);
		
		ServiceAccount sa3 = ServiceAccount.builder().id(UUID.randomUUID()).build();
		char[] a = "password2".toCharArray();
		Throwable exception = assertThrows(ArgosError.class, () -> {
			serviceAccountProviderService.setServiceAccountPassword(sa3, a);
        });
		assertEquals(String.format("Service Account [%s] not found in saprovider", sa3.getId().toString()), exception.getMessage());
		
	}
	
	@Test
	void testGetIdToken() {
		ServiceAccount sa2 = serviceAccountProviderService.registerServiceAccount(sa);
		assertEquals(sa.getId(), sa2.getId());
		assertNotNull(sa2.getProviderSubject());
		serviceAccountProviderService.setServiceAccountPassword(sa2, "password1".toCharArray());
		String token = serviceAccountProviderService.getIdToken(sa2.getId(), "password1".toCharArray());
		assertNotNull(token);
		char[] a = "other".toCharArray();
		UUID id = sa2.getId();
		Throwable exception = assertThrows(NotAuthorizedException.class, () -> {
			serviceAccountProviderService.getIdToken(id, a);
        });
		assertEquals("HTTP 401 Unauthorized", exception.getMessage());
	}

}
