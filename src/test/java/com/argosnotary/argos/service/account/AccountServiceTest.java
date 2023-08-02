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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.argosnotary.argos.domain.account.Account;
import com.argosnotary.argos.domain.account.PersonalAccount;
import com.argosnotary.argos.domain.crypto.CryptoHelper;
import com.argosnotary.argos.domain.crypto.KeyPair;
import com.argosnotary.argos.service.mongodb.account.PersonalAccountRepository;
import com.argosnotary.argos.service.mongodb.account.ServiceAccountRepository;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {
	
	private AccountService accountService; 
	
	private PersonalAccount pa1;
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
	}

	@Test
	void testKeyPairExists() {
		when(personalAccountRepository.existsByActiveKey(kp1.getKeyId())).thenReturn(true);
		when(serviceAccountRepository.existsByActiveKey(kp1.getKeyId())).thenReturn(false);
		assertTrue(accountService.keyPairExists(kp1.getKeyId()));
		verify(personalAccountRepository).existsByActiveKey(kp1.getKeyId());
		verify(serviceAccountRepository).existsByActiveKey(kp1.getKeyId());
	}

	@Test
	void testFindKeyPairByKeyId() {
		when(personalAccountRepository.findFirstByActiveKeyId(kp1.getKeyId())).thenReturn(Optional.of(pa1));
		when(serviceAccountRepository.findFirstByActiveKeyId(kp1.getKeyId())).thenReturn(Optional.empty());
		assertThat(accountService.findKeyPairByKeyId(kp1.getKeyId()), is(Optional.of(kp1)));
		verify(personalAccountRepository).findFirstByActiveKeyId(kp1.getKeyId());
		verify(serviceAccountRepository).findFirstByActiveKeyId(kp1.getKeyId());
		
	}

	@Test
	void testLoadAuthenticatedUser() {
		Optional<String> optProviderName = Optional.of("optProviderName");
		when(clientRegistrationService.getClientRegistrationName("provider1")).thenReturn(optProviderName);
		when(personalAccountService.findByProviderNameAndProviderSubject(optProviderName.get(), "subject1")).thenReturn(Optional.of(pa1));
		
		Optional<Account> acc = accountService.loadAuthenticatedUser("provider1","subject1");
		assertEquals(pa1, acc.get());
		
	}

}
