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
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.argosnotary.argos.domain.account.ServiceAccount;
import com.argosnotary.argos.domain.crypto.CryptoHelper;
import com.argosnotary.argos.domain.crypto.KeyPair;
import com.argosnotary.argos.service.itest.mongodb.ArgosTestContainers;


@Testcontainers
@DataMongoTest(properties={"spring.data.mongodb.auto-index-creation=true","spring.data.mongodb.database=argos"})
class ServiceAccountRepositoryTest {
	
	@Container //
	private static MongoDBContainer mongoDBContainer = ArgosTestContainers.getMongoDBContainer();
	
	@DynamicPropertySource
	static void setProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
	}

	@Autowired ServiceAccountRepository serviceAccountRepository;
	
	private ServiceAccount sa1, sa2;
	private KeyPair kp1, kp2, kp3;
	private UUID projectId;

	@BeforeEach
	void setUp() throws Exception {
		kp1 = CryptoHelper.createKeyPair("wachtwoord".toCharArray());
		kp2 = CryptoHelper.createKeyPair("wachtwoord".toCharArray());
		kp3 = CryptoHelper.createKeyPair("wachtwoord".toCharArray());
		projectId = UUID.randomUUID(); 
		sa1 = ServiceAccount.builder().name("sa1").activeKeyPair(kp1).projectId(projectId).providerSubject("subject1").build();
		sa2 = ServiceAccount.builder().name("sa2").activeKeyPair(kp2).projectId(projectId).providerSubject("subject2").build();
		
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
		assertThat(serviceAccountRepository.findFirstByActiveKeyId(kp1.getKeyId()).get(), is(sa1));
		assertThat(serviceAccountRepository.findFirstByActiveKeyId(kp2.getKeyId()).get(), is(sa2));

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
