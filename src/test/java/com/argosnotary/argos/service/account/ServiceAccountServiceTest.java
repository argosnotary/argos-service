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
package com.argosnotary.argos.service.account;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.argosnotary.argos.domain.account.ServiceAccount;
import com.argosnotary.argos.domain.crypto.CryptoHelper;
import com.argosnotary.argos.domain.crypto.KeyPair;
import com.argosnotary.argos.service.mongodb.account.ServiceAccountRepository;

import jakarta.ws.rs.NotAuthorizedException;

@ExtendWith(MockitoExtension.class)
class ServiceAccountServiceTest {
	
	private ServiceAccountService serviceAccountService; 
	
	private ServiceAccount sa1;
	private KeyPair kp1, kp2;
	
    @Mock
    private ServiceAccountRepository serviceAccountRepository;
    
    @Mock
    private ServiceAccountProviderService serviceAccountProviderService;

	@BeforeEach
	void setUp() throws Exception {
		serviceAccountService = new ServiceAccountServiceImpl(serviceAccountRepository,serviceAccountProviderService);
		kp1 = CryptoHelper.createKeyPair("wachtwoord".toCharArray());
		kp2 = CryptoHelper.createKeyPair("wachtwoord".toCharArray());
		sa1 = ServiceAccount.builder().name("sa1").activeKeyPair(kp1).providerSubject("subject1").build();
	}
	
	@Test
	void testCreateServiceAccount() {
		serviceAccountService.createServiceAccount(sa1);
		verify(serviceAccountRepository).insert(sa1);
		verify(serviceAccountProviderService).registerServiceAccount(sa1);
	}

	@Test
	void testActivateNewKey() {
		ServiceAccount expected = ServiceAccount.builder().name("sa1").id(sa1.getId()).providerSubject("subject1").activeKeyPair(kp1).build();
		expected.deactivateKeyPair(kp2);
		serviceAccountService.activateNewKey(sa1, kp2, "wachtwoord".toCharArray());
		verify(serviceAccountRepository).save(expected);
		verify(serviceAccountProviderService).setServiceAccountPassword(expected, "wachtwoord".toCharArray());
	}
	
	@Test
	void testGetIdTokenAuth() {
		ServiceAccount sa = ServiceAccount.builder().name("sa1").id(sa1.getId()).providerSubject("subject1").activeKeyPair(kp1).build();
		when(serviceAccountProviderService.getIdToken(sa.getId(), "wachtwoord".toCharArray())).thenReturn("token");
		String token = serviceAccountService.getIdToken(sa, "wachtwoord".toCharArray());
		assertEquals("token", token);
	}
	
	@Test
	void testGetIdTokenNotAuth() {
		ServiceAccount sa = ServiceAccount.builder().name("sa1").id(sa1.getId()).providerSubject("subject1").activeKeyPair(kp1).build();
		when(serviceAccountProviderService.getIdToken(sa.getId(), "wachtwoord".toCharArray())).thenThrow(NotAuthorizedException.class);
		String token = null;
		char[] a = "wachtwoord".toCharArray();
		Throwable exception = assertThrows(NotAuthorizedException.class, () -> {
			serviceAccountService.getIdToken(sa, a);
        });
		assertNull(token);
	}
	
	@Test
	void testDelete() {
		serviceAccountService.delete(sa1);
		verify(serviceAccountRepository).deleteById(sa1.getId());
		verify(serviceAccountProviderService).unRegisterServiceAccount(sa1);
	}
	
	@Test
	void testFindById() {
		when(serviceAccountRepository.findById(sa1.getId())).thenReturn(Optional.of(sa1));
		Optional<ServiceAccount> sa = serviceAccountService.findById(sa1.getId());
		assertEquals(sa1, sa.get());
	}
	
	@Test
	void testFindByIdNotFound() {
		when(serviceAccountRepository.findById(sa1.getId())).thenReturn(Optional.empty());
		Optional<ServiceAccount> sa = serviceAccountService.findById(sa1.getId());
		assertTrue(sa.isEmpty());
	}

	@Test
	void testFindByProviderSubject() {
		when(serviceAccountRepository.findFirstByProviderSubject("subject")).thenReturn(Optional.of(sa1));
		Optional<ServiceAccount> sa = serviceAccountService.findByProviderSubject("subject");
		assertEquals(sa1, sa.get());
		
	}

	@Test
	void testFindByProviderSubjectNotFound() {
		when(serviceAccountRepository.findFirstByProviderSubject("subject")).thenReturn(Optional.empty());
		Optional<ServiceAccount> sa = serviceAccountService.findByProviderSubject("subject");
		assertTrue(sa.isEmpty());
		
	}
    
	@ParameterizedTest
	@CsvSource({
		"true, false, false",
		"false, true, false",
		"true, true, true"
		})
	void testExistsById(boolean sp, boolean sr, boolean result) {
		when(serviceAccountProviderService.exists(sa1.getId())).thenReturn(sp);
		if (sp)
			when(serviceAccountRepository.existsById(sa1.getId())).thenReturn(sr);
		assertThat(serviceAccountService.exists(sa1.getId()), is(result));
	}
    
	@Test
	void testExistsByProjectIdAndName() {
		UUID pId = UUID.randomUUID();
		when(serviceAccountRepository.existsByProjectIdAndName(pId, sa1.getName())).thenReturn(true);
		assertTrue(serviceAccountService.exists(pId, sa1.getName()));
		
	}
}
