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
package com.argosnotary.argos.service.account;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistration.ProviderDetails;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;

import com.argosnotary.argos.domain.ArgosError;

@ExtendWith(MockitoExtension.class)
class ClientRegistrationServiceTest {
	
	ClientRegistrationService clientRegistrationService;
	@Mock
	ClientRegistration saprovider;
	@Mock
	ClientRegistration oauthstub;
	@Mock
	ClientRegistration master;
	@Mock
	ClientRegistration wrong;
	
	@Mock
	ProviderDetails saproviderProviderDetails;
	
	@Mock
	ProviderDetails oauthStubProviderDetails;
	
	@Mock
	ProviderDetails masterProviderDetails;
	
	@Mock
	ProviderDetails wrongProviderDetails;
	
	List<ClientRegistration> registrations = new ArrayList<>();
		
	InMemoryClientRegistrationRepository clientRegistrationRepository;

	@BeforeEach
	void setUp() throws Exception {
		when(saprovider.getRegistrationId()).thenReturn("saprovider");
		when(oauthstub.getRegistrationId()).thenReturn("oauth-stub");
		when(master.getRegistrationId()).thenReturn("master");
		when(wrong.getRegistrationId()).thenReturn("wrong");
		when(saprovider.getProviderDetails()).thenReturn(saproviderProviderDetails);
		when(oauthstub.getProviderDetails()).thenReturn(oauthStubProviderDetails);
		when(master.getProviderDetails()).thenReturn(masterProviderDetails);
		when(wrong.getProviderDetails()).thenReturn(wrongProviderDetails);
		when(saproviderProviderDetails.getIssuerUri()).thenReturn("http://localhost:8080/saprovider");
		when(oauthStubProviderDetails.getIssuerUri()).thenReturn("http://localhost/oauth-stub");
		when(masterProviderDetails.getIssuerUri()).thenReturn("http://localhost/master");
		when(wrongProviderDetails.getIssuerUri()).thenReturn("//localhost/wrong");
		registrations.addAll(List.of(saprovider, oauthstub, master, wrong));
		clientRegistrationRepository = new InMemoryClientRegistrationRepository(registrations);
		clientRegistrationService = new ClientRegistrationServiceImpl(clientRegistrationRepository);
	}
	
	@Test
	void testGetClientRegistrationName() {
		Optional<String> reg = clientRegistrationService.getClientRegistrationNameWithIssuer("http://localhost:8080/saprovider");
		assertEquals("saprovider", reg.get());
		reg = clientRegistrationService.getClientRegistrationNameWithIssuer("http://localhost/anders");
		assertTrue(reg.isEmpty());
	}

	@Test
	void testGetClientRegistration() {
		Optional<ClientRegistration> reg = clientRegistrationService.getClientRegistration("saprovider");
		assertEquals(saprovider, reg.get());
		reg = clientRegistrationService.getClientRegistration("anders");
		assertTrue(reg.isEmpty());
	}

	@Test
	void testGetAllOauthIssuers() {
		Set<String> iss = clientRegistrationService.getAllOauthIssuers();
		assertThat(iss, is(registrations.stream().map(r -> r.getProviderDetails().getIssuerUri()).collect(Collectors.toSet())));
	}
	
	@Test
	void testGetAllExternalProviderIds() {
		List<OAuthProvider> ps = clientRegistrationService.getAllExternalProviderIds();
		assertThat(ps, is(List.of(new OAuthProvider("oauth-stub"), new OAuthProvider("wrong"))));
	}
	
	@Test
	void testGetClientRegistrationProviderUrl() {
		Optional<String> url = clientRegistrationService.getClientRegistrationProviderUrl("saprovider");
		assertEquals("http://localhost:8080", url.get());
		url = clientRegistrationService.getClientRegistrationProviderUrl("other");
		assertTrue(url.isEmpty());
		url = clientRegistrationService.getClientRegistrationProviderUrl("other");

        
        Throwable exception = assertThrows(ArgosError.class, () -> {
        	clientRegistrationService.getClientRegistrationProviderUrl("wrong");
          });
        
        assertEquals("no protocol: //localhost/wrong", exception.getMessage());
	}
	
	@Test
	void testExists() {
		assertTrue(clientRegistrationService.exists("saprovider"));
		assertFalse(clientRegistrationService.exists("other"));
	}

	@Test
	void testGetServiceAccountIssuer() {
		assertEquals("http://localhost:8080/saprovider", clientRegistrationService.getServiceAccountIssuer());
	}

}
