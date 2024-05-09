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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.argosnotary.argos.domain.account.PersonalAccount;
import com.argosnotary.argos.domain.crypto.CryptoHelper;
import com.argosnotary.argos.domain.crypto.KeyPair;
import com.argosnotary.argos.service.mongodb.account.PersonalAccountRepository;

@ExtendWith(MockitoExtension.class)
class PersonalAccountServiceTest {
	
	private PersonalAccountService personalAccountService; 
	
	private PersonalAccount pa1, pa1NewKey;
	private KeyPair kp1, kp2;
    @Mock
    private PersonalAccountRepository personalAccountRepository;

	@BeforeEach
	void setUp() throws Exception {
		personalAccountService = new PersonalAccountServiceImpl(personalAccountRepository);
		kp1 = CryptoHelper.createKeyPair("wachtwoord".toCharArray());
		kp2 = CryptoHelper.createKeyPair("wachtwoord".toCharArray());
		pa1 = PersonalAccount.builder().name("pa1").activeKeyPair(kp1).providerName("provider1").providerSubject("subject1").build();
		pa1NewKey = PersonalAccount.builder().id(pa1.getId()).name("pa1").activeKeyPair(kp2).inactiveKeyPairs(Set.of(kp1)).providerName("provider1").providerSubject("subject1").build();
	}

	@Test
	void testActivateNewKey() {
		when(personalAccountRepository.save(pa1NewKey)).thenReturn(pa1NewKey);
		PersonalAccount pa = personalAccountService.activateNewKey(pa1, kp2);
		assertEquals(pa1NewKey, pa);
		
		
	}
	
	@Test
	void testGetPersonalAccountById() {
		when(personalAccountRepository.findById(pa1.getId())).thenReturn(Optional.of(pa1));
		Optional<PersonalAccount> pa = personalAccountService.getPersonalAccountById(pa1.getId());
		assertEquals(pa.get(), pa1);
	}
	
	@Test
	void testGetPersonalAccountByIdEmpty() {
		when(personalAccountRepository.findById(pa1.getId())).thenReturn(Optional.empty());
		Optional<PersonalAccount> pa = personalAccountService.getPersonalAccountById(pa1.getId());
		assertTrue(pa.isEmpty());
	}

	@Test
	void testFindByProviderNameAndProviderSubject() {
		when(personalAccountRepository.findFirstByProviderNameAndProviderSubject("providerName", "paSubject")).thenReturn(Optional.of(pa1));
		Optional<PersonalAccount> pa = personalAccountService.findByProviderNameAndProviderSubject("providerName", "paSubject");
		assertEquals(pa.get(), pa1);
	}

	@Test
	void testFindByProviderNameAndProviderSubjectEmpty() {
		when(personalAccountRepository.findFirstByProviderNameAndProviderSubject("providerName", "paSubject")).thenReturn(Optional.empty());
		Optional<PersonalAccount> pa = personalAccountService.findByProviderNameAndProviderSubject("providerName", "paSubject");
		assertTrue(pa.isEmpty());
	}

	@Test
	void testSave() {
		when(personalAccountRepository.save(pa1)).thenReturn(pa1);
		PersonalAccount pa = personalAccountService.save(pa1);
		assertEquals(pa1, pa);
	}

}
