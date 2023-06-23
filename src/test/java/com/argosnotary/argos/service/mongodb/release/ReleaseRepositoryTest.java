package com.argosnotary.argos.service.mongodb.release;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

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

import com.argosnotary.argos.domain.nodes.Domain;
import com.argosnotary.argos.domain.nodes.Organization;
import com.argosnotary.argos.domain.release.Release;
import com.argosnotary.argos.service.itest.mongodb.ArgosTestContainers;

@Testcontainers
@DataMongoTest(properties= {"spring.data.mongodb.auto-index-creation=true"})
class ReleaseRepositoryTest {

	@Container //
	private static MongoDBContainer mongoDBContainer = ArgosTestContainers.getMongoDBContainer();

	@DynamicPropertySource
	static void setProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
	}
	
	private Release r11, r12, r21, r22, r23;
	private Organization org1, org2;

	@Autowired 
	ReleaseRepository releaseRepository;

	@BeforeEach
	void setUp() throws Exception {
		org1 = new Organization(UUID.randomUUID(), "org1", Domain.builder().domain("domainName1").build());

		org2 = new Organization(UUID.randomUUID(), "org2", Domain.builder().domain("domainName1").build());
		
		releaseRepository.deleteAll();
		r11 = Release.builder().supplyChainId(UUID.randomUUID()).id(UUID.randomUUID()).organization(org1).releasedProductsHashes(Set.of("hash111","hash112","hash113")).build();
		r12 = Release.builder().supplyChainId(r11.getSupplyChainId()).id(UUID.randomUUID()).organization(org1).releasedProductsHashes(Set.of("hash121","hash122","hash123")).build();
		r21 = Release.builder().supplyChainId(UUID.randomUUID()).id(UUID.randomUUID()).organization(org2).releasedProductsHashes(Set.of("hash211","hash212","hash213")).build();
		r22 = Release.builder().supplyChainId(UUID.randomUUID()).id(UUID.randomUUID()).organization(org2).releasedProductsHashes(Set.of("hash221","hash222","hash223")).build();
		r23 = Release.builder().supplyChainId(UUID.randomUUID()).id(UUID.randomUUID()).organization(org2).releasedProductsHashes(Set.of("hash231","hash232","hash233")).build();
		r11.setReleasedProductsHashesHash(Release.calculateReleasedProductsHashesHash(r11.getReleasedProductsHashes()));
		r12.setReleasedProductsHashesHash(Release.calculateReleasedProductsHashesHash(r12.getReleasedProductsHashes()));
		r21.setReleasedProductsHashesHash(Release.calculateReleasedProductsHashesHash(r21.getReleasedProductsHashes()));
		r22.setReleasedProductsHashesHash(Release.calculateReleasedProductsHashesHash(r22.getReleasedProductsHashes()));
		r23.setReleasedProductsHashesHash(Release.calculateReleasedProductsHashesHash(r23.getReleasedProductsHashes()));
		releaseRepository.insert(Set.of(r11, r12,r21,r22,r23));
		
	}

	@Test
	void testArtifactsNotReleased() {
		Release r = releaseRepository.artifactsNotReleased(List.of("domainName1"), Set.of("hash111", "hash114"));
		assertEquals(r.getReleasedProductsHashes(), Set.of("hash114"));
	}
	
	@Test
	void testFindByReleasedProductsHashesHashAndSupplyChainId() {
		Optional<Release> release = releaseRepository.findByReleasedProductsHashesHashAndSupplyChainId(r11.getReleasedProductsHashesHash(), r11.getSupplyChainId());
		assertThat(release.isPresent(), is(true));
		assertEquals(r11, release.get());
	}
	
	@Test
	void testFindByReleasedProductsHashesHashAndSupplyChainIdNotFound() {
		Optional<Release> release = releaseRepository.findByReleasedProductsHashesHashAndSupplyChainId(r11.getReleasedProductsHashesHash(), r21.getSupplyChainId());
		assertThat(release.isEmpty(), is(true));
	}

}
