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

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.EnabledIf;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.argosnotary.argos.domain.account.ServiceAccount;
import com.argosnotary.argos.domain.crypto.CryptoHelper;
import com.argosnotary.argos.domain.crypto.KeyPair;
import com.argosnotary.argos.domain.crypto.PublicKey;
import com.argosnotary.argos.service.ArgosTestContainers;


@Testcontainers
@DataMongoTest(properties={"spring.data.mongodb.auto-index-creation=true","spring.data.mongodb.database=argos"})
class ServiceAccountRepositoryTest {
	
	static MongoDBContainer mongoDBContainer = ArgosTestContainers.getMongoDBContainer();
    
    static {
        mongoDBContainer.start();
    }
	
	@DynamicPropertySource
	static void setProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
	}

	@Autowired ServiceAccountRepository serviceAccountRepository;
	
	private ServiceAccount sa1, sa2, sa1NoKey, sa2NoKey;
	private KeyPair kp1, kp2, kp3, kp1NoKey, kp2NoKey;
	private UUID projectId;

	@BeforeEach
	void setUp() throws Exception {
		kp1 = CryptoHelper.createKeyPair("wachtwoord".toCharArray());
		kp2 = CryptoHelper.createKeyPair("wachtwoord".toCharArray());
		kp3 = CryptoHelper.createKeyPair("wachtwoord".toCharArray());
		projectId = UUID.randomUUID(); 
		sa1 = ServiceAccount.builder().name("sa1").activeKeyPair(kp1).projectId(projectId).providerSubject("subject1").build();
		sa2 = ServiceAccount.builder().name("sa2").activeKeyPair(kp2).projectId(projectId).providerSubject("subject2").build();
		
		kp1NoKey = new KeyPair(kp1.getKeyId(),kp1.getPub(), null);
		kp2NoKey = new KeyPair(kp2.getKeyId(),kp2.getPub(), null);
		sa1NoKey = ServiceAccount.builder().id(sa1.getId()).name("sa1").activeKeyPair(kp1NoKey).projectId(projectId).providerSubject("subject1").build();
		sa2NoKey = ServiceAccount.builder().id(sa2.getId()).name("sa2").activeKeyPair(kp2NoKey).projectId(projectId).providerSubject("subject2").build();
		
		
		serviceAccountRepository.deleteAll();
		
		serviceAccountRepository.save(sa1);
		serviceAccountRepository.save(sa2);
		
		
		
		
	}

	@Test
	void testExistsServiceAccountByActiveKey() {
		assertTrue(serviceAccountRepository.existsByActiveKey(kp1.getKeyId()));

		assertFalse(serviceAccountRepository.existsByActiveKey(kp3.getKeyId()));
	}
	
	@Test
	void testFindByActiveKeyId() {
		assertThat(serviceAccountRepository.findFirstByActiveKeyId(kp1.getKeyId()).get(), is(sa1NoKey));
		assertThat(serviceAccountRepository.findFirstByActiveKeyId(kp2.getKeyId()).get(), is(sa2NoKey));

		assertTrue(serviceAccountRepository.findFirstByActiveKeyId(kp3.getKeyId()).isEmpty());
		
	}
	
	@Test
	void testFindByProviderNameAndProviderSubject() {
		assertThat(serviceAccountRepository.findFirstByProviderSubject("subject1").get(), is(sa1));

		assertTrue(serviceAccountRepository.findFirstByProviderSubject("zomaar wat").isEmpty());
		
	}
	
	@Test
	void testFindByProjectId() {
		assertThat(serviceAccountRepository.findByProjectId(projectId), containsInAnyOrder(sa1, sa2));
		assertThat(serviceAccountRepository.findByProjectId(UUID.randomUUID()).size(), is(0));
	}
    
	@Test
	void testDeleteByProjectId() {
		serviceAccountRepository.deleteByProjectId(projectId);
		assertThat(serviceAccountRepository.findByProjectId(projectId).size(), is(0));
		
	}
	
	@Test
	void testExists() {
		assertTrue(serviceAccountRepository.existsByProjectIdAndName(sa2.getProjectId(), sa2.getName()));
	}
	
	@Test
	void testUniqueNamePerProject() {
		ServiceAccount sa2 = ServiceAccount.builder().name("sa2").activeKeyPair(kp2).projectId(projectId).providerSubject("subject2").build();
		Throwable exception = assertThrows(DuplicateKeyException.class, () -> {
			serviceAccountRepository.insert(sa2);
          });
        
        assertThat(exception.getMessage(), containsString("duplicate key error"));
		
		
	}

}
