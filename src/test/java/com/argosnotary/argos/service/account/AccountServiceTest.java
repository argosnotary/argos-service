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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.argosnotary.argos.domain.account.Account;
import com.argosnotary.argos.domain.account.PersonalAccount;
import com.argosnotary.argos.domain.account.ServiceAccount;
import com.argosnotary.argos.domain.crypto.CryptoHelper;
import com.argosnotary.argos.domain.crypto.KeyPair;
import com.argosnotary.argos.domain.crypto.PublicKey;
import com.argosnotary.argos.service.mongodb.account.PersonalAccountRepository;
import com.argosnotary.argos.service.mongodb.account.ServiceAccountRepository;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {
	
	private AccountService accountService; 
	
	private PersonalAccount pa1;
	private ServiceAccount sa1;
	private KeyPair kp1, kp2;

    @Mock
	private ServiceAccountRepository serviceAccountRepository;
    @Mock
    private PersonalAccountRepository personalAccountRepository;
    @Mock
    private ClientRegistrationService clientRegistrationService;
    @Mock
    private ServiceAccountService serviceAccountService;
    @Mock
    private PersonalAccountService personalAccountService;

	@BeforeEach
	void setUp() throws Exception {
		accountService = new AccountServiceImpl(serviceAccountRepository,personalAccountRepository,clientRegistrationService,serviceAccountService, personalAccountService);
		kp1 = CryptoHelper.createKeyPair("wachtwoord".toCharArray());
		kp2 = CryptoHelper.createKeyPair("wachtwoord".toCharArray());
		pa1 = PersonalAccount.builder().name("pa1").activeKeyPair(kp1).providerName("provider1").providerSubject("subject1").build();
		sa1 = ServiceAccount.builder().name("sa1").providerSubject("sa1Subject").build();
	}

	@ParameterizedTest
	@CsvSource({
		"true, false, true",
		"false, true, true",
		"false, false, false"
		})
	void testKeyPairExists(boolean pa, boolean sa, boolean result) {
		when(serviceAccountRepository.existsByActiveKey(kp1.getKeyId())).thenReturn(sa);
		if (!sa) {
			when(personalAccountRepository.existsByActiveKey(kp1.getKeyId())).thenReturn(pa);
		}
		assertThat(accountService.keyPairExists(kp1.getKeyId()), is(result));
	}

	@Test
	void testFindPublicKeyByKeyId() {
		when(personalAccountRepository.findFirstByActiveKeyId(kp1.getKeyId())).thenReturn(Optional.of(pa1));
		when(serviceAccountRepository.findFirstByActiveKeyId(kp1.getKeyId())).thenReturn(Optional.empty());
		assertThat(accountService.findPublicKeyByKeyId(kp1.getKeyId()), is(Optional.of((PublicKey)kp1)));
		verify(personalAccountRepository).findFirstByActiveKeyId(kp1.getKeyId());
		verify(serviceAccountRepository).findFirstByActiveKeyId(kp1.getKeyId());
		
	}
	
	@Test
	void testFindByKeyIds() {
		when(personalAccountRepository.findByKeyIds(Set.of(kp1.getKeyId()))).thenReturn(new ArrayList<>());
		when(serviceAccountRepository.findByKeyIds(Set.of(kp1.getKeyId()))).thenReturn(List.of(sa1));
		assertThat(accountService.findByKeyIds(Set.of(kp1.getKeyId())), is(List.of(sa1)));
	}
	
	@Test
	void testFindByKeyIdsReverse() {
		when(personalAccountRepository.findByKeyIds(Set.of(kp1.getKeyId()))).thenReturn(new ArrayList<>(List.of(sa1)));
		when(serviceAccountRepository.findByKeyIds(Set.of(kp1.getKeyId()))).thenReturn(List.of());
		assertThat(accountService.findByKeyIds(Set.of(kp1.getKeyId())), is(List.of(sa1)));
	}

	@Test
	void testLoadAuthenticatedUser() {
		Optional<String> optProviderName = Optional.of("optProviderName");
		when(clientRegistrationService.getClientRegistrationNameWithIssuer("optProviderIssuer")).thenReturn(optProviderName);
		when(clientRegistrationService.getServiceAccountIssuer()).thenReturn("saProviderIssuer");
		when(personalAccountService.findByProviderNameAndProviderSubject(optProviderName.get(), "subject1")).thenReturn(Optional.of(pa1));
		
		Optional<Account> acc = accountService.loadAuthenticatedUser("optProviderIssuer","subject1");
		assertEquals(pa1, acc.get());
		
		when(serviceAccountService.findByProviderSubject("sa1Subject")).thenReturn(Optional.of(sa1));
		when(clientRegistrationService.getClientRegistrationNameWithIssuer("saProviderIssuer")).thenReturn(Optional.of("saprovider"));
		
		acc = accountService.loadAuthenticatedUser("saProviderIssuer","sa1Subject");
		assertEquals(sa1, acc.get());
		
	}

	@Test
	void testLoadAuthenticatedUserNoProvider() {

		when(clientRegistrationService.getClientRegistrationNameWithIssuer("provider1")).thenReturn(Optional.empty());
		
		Optional<Account> acc = accountService.loadAuthenticatedUser("provider1","subject1");
		assertTrue(acc.isEmpty());
		
	}

	@Test
	void testLoadAuthenticatedUserAccNotFound() {
		Optional<String> optProviderName = Optional.of("optProviderName");
		when(clientRegistrationService.getClientRegistrationNameWithIssuer("optProviderIssuer")).thenReturn(optProviderName);
		when(clientRegistrationService.getServiceAccountIssuer()).thenReturn("saProviderIssuer");
		when(personalAccountService.findByProviderNameAndProviderSubject(optProviderName.get(), "subject1")).thenReturn(Optional.empty());
		
		Optional<Account> acc = accountService.loadAuthenticatedUser("optProviderIssuer","subject1");
		assertTrue(acc.isEmpty());
		
		when(serviceAccountService.findByProviderSubject("sa1Subject")).thenReturn(Optional.empty());
		when(clientRegistrationService.getClientRegistrationNameWithIssuer("saProviderIssuer")).thenReturn(Optional.of("saprovider"));
		
		acc = accountService.loadAuthenticatedUser("saProviderIssuer","sa1Subject");
		assertTrue(acc.isEmpty());
		
	}

}
