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
package com.argosnotary.argos.service.mongodb.account;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.EnabledIf;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.argosnotary.argos.domain.account.PersonalAccount;
import com.argosnotary.argos.domain.account.PersonalAccount.Profile;
import com.argosnotary.argos.domain.crypto.CryptoHelper;
import com.argosnotary.argos.domain.crypto.KeyPair;
import com.argosnotary.argos.service.ArgosTestContainers;


@Testcontainers
@DataMongoTest
class PersonalAccountRepositoryTest {
	
	static MongoDBContainer mongoDBContainer = ArgosTestContainers.getMongoDBContainer();
    
    static {
        mongoDBContainer.start();
    }
	
	@DynamicPropertySource
	static void setProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
	}

	@Autowired PersonalAccountRepository personalAccountRepository;
	
	private PersonalAccount pa1, pa1WithoutProfileAndKey;
	private KeyPair kp1, kp2, kp1NoKey, kp2NoKey;

	@BeforeEach
	void setUp() throws Exception {
		kp1 = CryptoHelper.createKeyPair("wachtwoord".toCharArray());
		kp2 = CryptoHelper.createKeyPair("wachtwoord".toCharArray());
		
		kp1NoKey = new KeyPair(kp1.getKeyId(),kp1.getPublicKey(), null);
		kp2NoKey = new KeyPair(kp2.getKeyId(),kp2.getPublicKey(), null);
		
		pa1 = PersonalAccount.builder().id(UUID.randomUUID()).name("pa1").profile(Profile.builder().build()).activeKeyPair(kp1).providerName("provider1").providerSubject("subject1").build();
		pa1WithoutProfileAndKey = PersonalAccount.builder().id(pa1.getId()).name("pa1").activeKeyPair(kp1NoKey).providerName("provider1").providerSubject("subject1").build();
		
		personalAccountRepository.deleteAll();
		personalAccountRepository.save(pa1);
		
		
		
		
	}

	@Test
	void testExistsPersonalAccountByActiveKey() {
		assertTrue(personalAccountRepository.existsByActiveKey(kp1.getKeyId()));

		assertFalse(personalAccountRepository.existsByActiveKey(kp2.getKeyId()));
	}
	
	@Test
	void testFindFirstByActiveKeyId() {
		assertThat(personalAccountRepository.findFirstByActiveKeyId(kp1.getKeyId()).get(), is(pa1WithoutProfileAndKey));

		assertTrue(personalAccountRepository.findFirstByActiveKeyId(kp2.getKeyId()).isEmpty());
		
	}
	
	@Test
	void testFindByKeyId() {
		assertThat(personalAccountRepository.findByKeyIds(Set.of(kp1.getKeyId())), is(List.of(pa1WithoutProfileAndKey)));
		
	}
	
	@Test
	void testFindByProviderNameAndProviderSubject() {
		assertThat(personalAccountRepository.findFirstByProviderNameAndProviderSubject("provider1", "subject1").get(), is(pa1));

		assertTrue(personalAccountRepository.findFirstByProviderNameAndProviderSubject("provider1", "zomaar wat").isEmpty());
		
	}

}
