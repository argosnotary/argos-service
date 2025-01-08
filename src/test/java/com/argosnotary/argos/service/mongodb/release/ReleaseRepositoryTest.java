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
package com.argosnotary.argos.service.mongodb.release;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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

import com.argosnotary.argos.domain.nodes.Domain;
import com.argosnotary.argos.domain.release.Release;
import com.argosnotary.argos.service.ArgosTestContainers;

@Testcontainers
@DataMongoTest(properties= {"spring.data.mongodb.auto-index-creation=true"})
class ReleaseRepositoryTest {

	static MongoDBContainer mongoDBContainer = ArgosTestContainers.getMongoDBContainer();
    
    static {
        mongoDBContainer.start();
    }

	@DynamicPropertySource
	static void setProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
	}
	
	private UUID sc11 = UUID.randomUUID();
	private UUID sc12 = UUID.randomUUID();
	private UUID sc21 = UUID.randomUUID();
	private UUID sc31 = UUID.randomUUID();
	
	private Release r111, r112, r121, r211, r212, r213, r311;
	private Domain domain1, domain2, domain3;

	@Autowired 
	ReleaseRepository releaseRepository;

	@BeforeEach
	void setUp() throws Exception {
		domain1 = Domain.builder().name("org1.com").build();
		domain2 = Domain.builder().name("org2.com").build();
		domain3 = Domain.builder().name("org3.com").build();
		
		releaseRepository.deleteAll();
		r111 = Release.builder().name("r111").supplyChainId(sc11).id(UUID.randomUUID()).domain(domain1).releasedProductsHashes(Set.of("hash1","hash2","hash3")).build();
		r112 = Release.builder().name("r112").supplyChainId(sc11).id(UUID.randomUUID()).domain(domain1).releasedProductsHashes(Set.of("hash1","hash2","hash4")).build();
		r121 = Release.builder().name("r121").supplyChainId(sc12).id(UUID.randomUUID()).domain(domain1).releasedProductsHashes(Set.of("hash5","hash6","hash7")).build();
		r211 = Release.builder().name("r211").supplyChainId(sc21).id(UUID.randomUUID()).domain(domain2).releasedProductsHashes(Set.of("hash1","hash8","hash9")).build();
		r212 = Release.builder().name("r212").supplyChainId(sc21).id(UUID.randomUUID()).domain(domain2).releasedProductsHashes(Set.of("hash10","hash8","hash9")).build();
		r213 = Release.builder().name("r213").supplyChainId(sc21).id(UUID.randomUUID()).domain(domain2).releasedProductsHashes(Set.of("hash1","hash8","hash11")).build();
		r311 = Release.builder().name("r311").supplyChainId(sc31).id(UUID.randomUUID()).domain(domain3).releasedProductsHashes(Set.of("hash1","hash12","hash13")).build();
		r111.setReleasedProductsHashesHash(Release.calculateReleasedProductsHashesHash(r111.getReleasedProductsHashes()));
		r112.setReleasedProductsHashesHash(Release.calculateReleasedProductsHashesHash(r112.getReleasedProductsHashes()));
		r121.setReleasedProductsHashesHash(Release.calculateReleasedProductsHashesHash(r121.getReleasedProductsHashes()));
		r211.setReleasedProductsHashesHash(Release.calculateReleasedProductsHashesHash(r211.getReleasedProductsHashes()));
		r212.setReleasedProductsHashesHash(Release.calculateReleasedProductsHashesHash(r212.getReleasedProductsHashes()));
		r213.setReleasedProductsHashesHash(Release.calculateReleasedProductsHashesHash(r213.getReleasedProductsHashes()));
		r311.setReleasedProductsHashesHash(Release.calculateReleasedProductsHashesHash(r311.getReleasedProductsHashes()));
		releaseRepository.insert(Set.of(r111, r112,r121,r211, r212,r213,r311));
		
	}

	@Test
	void testFindByDomainNamesAndHashes() {
		assertFalse(releaseRepository.existsByDomainNamesAndHashes(List.of("anders"), Set.of("hash111", "hash114")));

		assertTrue(releaseRepository.existsByDomainNamesAndHashes(List.of("org1.com", "org2.com"), Set.of("hash1","hash2","hash3","hash12","hash13")));
		
		assertTrue(releaseRepository.existsByDomainNamesAndHashes(List.of("org1.com", "org2.com"), Set.of("hash1","hash2","hash3", "hash4", "hash5","hash6","hash7", "hash8","hash9", "hash10")));
		
	}
	
	@Test
	void testFindByReleasedProductsHashesHashAndSupplyChainId() {
		Optional<Release> release = releaseRepository.findByReleasedProductsHashesHashAndSupplyChainId(r111.getReleasedProductsHashesHash(), r111.getSupplyChainId());
		assertThat(release.isPresent(), is(true));
		assertEquals(r111, release.get());
	}
	
	@Test
	void testFindByReleasedProductsHashesHashAndSupplyChainIdNotFound() {
		Optional<Release> release = releaseRepository.findByReleasedProductsHashesHashAndSupplyChainId(r111.getReleasedProductsHashesHash(), r211.getSupplyChainId());
		assertThat(release.isEmpty(), is(true));
	}

}
