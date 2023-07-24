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

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.argosnotary.argos.domain.account.PersonalAccount;
import com.argosnotary.argos.domain.crypto.CryptoHelper;
import com.argosnotary.argos.domain.crypto.KeyPair;
import com.argosnotary.argos.service.account.PersonalAccountService;
import com.argosnotary.argos.service.account.PersonalAccountServiceImpl;
import com.argosnotary.argos.service.mongodb.account.PersonalAccountRepository;

@ExtendWith(MockitoExtension.class)
class PersonalAccountServiceTest {
	
	private PersonalAccountService personalAccountService; 
	
	private PersonalAccount pa1;
	private KeyPair kp1, kp2;
    @Mock
    private PersonalAccountRepository personalAccountRepository;

	@BeforeEach
	void setUp() throws Exception {
		personalAccountService = new PersonalAccountServiceImpl(personalAccountRepository);
		kp1 = CryptoHelper.createKeyPair("wachtwoord".toCharArray());
		kp2 = CryptoHelper.createKeyPair("wachtwoord".toCharArray());
		pa1 = PersonalAccount.builder().name("pa1").activeKeyPair(kp1).providerName("provider1").providerSubject("subject1").build();
	}

	@Test
	void testActivateNewKey() {
		personalAccountService.activateNewKey(pa1, kp2);
		pa1.deactivateKeyPair(kp2);
		verify(personalAccountRepository).save(pa1);
		
	}

}
