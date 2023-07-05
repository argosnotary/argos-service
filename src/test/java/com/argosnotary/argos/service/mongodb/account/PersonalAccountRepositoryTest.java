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
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.argosnotary.argos.domain.account.PersonalAccount;
import com.argosnotary.argos.domain.account.PersonalAccount.Profile;
import com.argosnotary.argos.domain.crypto.CryptoHelper;
import com.argosnotary.argos.domain.crypto.KeyPair;
import com.argosnotary.argos.service.itest.mongodb.ArgosTestContainers;


@Testcontainers
@DataMongoTest
class PersonalAccountRepositoryTest {
	
	@Container //
	private static MongoDBContainer mongoDBContainer = ArgosTestContainers.getMongoDBContainer();
	
	@DynamicPropertySource
	static void setProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
	}

	@Autowired PersonalAccountRepository personalAccountRepository;
	
	private PersonalAccount pa1, pa1WithoutProfile;
	private KeyPair kp1, kp2;

	@BeforeEach
	void setUp() throws Exception {
		kp1 = CryptoHelper.createKeyPair("wachtwoord".toCharArray());
		kp2 = CryptoHelper.createKeyPair("wachtwoord".toCharArray());
		
		pa1 = PersonalAccount.builder().id(UUID.randomUUID()).name("pa1").profile(Profile.builder().build()).activeKeyPair(kp1).providerName("provider1").providerSubject("subject1").build();
		pa1WithoutProfile = PersonalAccount.builder().id(pa1.getId()).name("pa1").activeKeyPair(kp1).providerName("provider1").providerSubject("subject1").build();
		
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
		assertThat(personalAccountRepository.findFirstByActiveKeyId(kp1.getKeyId()).get(), is(pa1WithoutProfile));
		
		Optional<PersonalAccount> optPa = personalAccountRepository.findFirstByActiveKeyId(kp1.getKeyId());

		assertTrue(personalAccountRepository.findFirstByActiveKeyId(kp2.getKeyId()).isEmpty());
		
	}
	
	@Test
	void testFindByKeyId() {
		assertThat(personalAccountRepository.findByKeyIds(Set.of(kp1.getKeyId())), is(List.of(pa1WithoutProfile)));
		
	}
	
	@Test
	void testFindByProviderNameAndProviderSubject() {
		assertThat(personalAccountRepository.findFirstByProviderNameAndProviderSubject("provider1", "subject1").get(), is(pa1));

		assertTrue(personalAccountRepository.findFirstByProviderNameAndProviderSubject("provider1", "zomaar wat").isEmpty());
		
	}

}
