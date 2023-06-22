/*
 * Argos Notary - A new way to secure the Software Supply Chain
 *
 * Copyright (C) 2019 - 2020 Rabobank Nederland
 * Copyright (C) 2019 - 2022 Gerard Borst <gerard.borst@argosnotary.com>
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
package com.argosnotary.argos.service.mongodb;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;

import java.io.IOException;
import java.util.List;
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

import com.argosnotary.argos.domain.crypto.Signature;
import com.argosnotary.argos.domain.link.Artifact;
import com.argosnotary.argos.domain.link.Link;
import com.argosnotary.argos.domain.link.LinkMetaBlock;
import com.argosnotary.argos.service.itest.mongodb.ArgosTestContainers;
import com.argosnotary.argos.service.mongodb.link.LinkMetaBlockRepository;

/*
 * Integration tests of all repositories exept the Release Repository
 */
@Testcontainers
@DataMongoTest
class LinkRepositoryTest {
	
	@Container //
	private static MongoDBContainer mongoDBContainer = ArgosTestContainers.getMongoDBContainer();

	@DynamicPropertySource
	static void setProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
	}

	@Autowired LinkMetaBlockRepository linkMetaBlockRepository;
	
	private static final String STEP_NAME = "stepName";
    private static final String STEP_NAME_NEW = "stepNameNew";
    private static final UUID SUPPLY_CHAIN_ID = UUID.randomUUID();
    private static final UUID OTHER_SUPPLY_CHAIN_ID = UUID.randomUUID();
    private static final String HASH_1 = "74a88c1cb96211a8f648af3509a1207b2d4a15c0202cfaa10abad8cc26300c63";
    private static final String HASH_2 = "1e6a4129c8b90e9b6c4727a59b1013d714576066ad1bad05034847f30ffb62b6";
    private static final String ARGOS_TEST_IML = "argos-test.iml";
    private static final String DOCKER_1_IML = "docker (1).iml";
	
    @BeforeEach
    void setup() throws IOException, ClassNotFoundException {
    	linkMetaBlockRepository.deleteAll();
        createDataSet();
    }
    
    @Test
    void findAll() {
        List<LinkMetaBlock> blocks = linkMetaBlockRepository.findAll();
        assertThat(blocks, hasSize(3));
    }
    
    @Test
    void findBySupplyChainId() {
        List<LinkMetaBlock> blocks = linkMetaBlockRepository.findBySupplyChainId(SUPPLY_CHAIN_ID);
        assertThat(blocks, hasSize(2));
    }
    
    @Test
    void deleteBySupplyChainId() {
        linkMetaBlockRepository.deleteBySupplyChainId(SUPPLY_CHAIN_ID);

        List<LinkMetaBlock> blocks = linkMetaBlockRepository.findAll();
        assertThat(blocks, hasSize(1));
    }
    

    void createDataSet() {
        linkMetaBlockRepository.save(LinkMetaBlock
                .builder()
                .supplyChainId(SUPPLY_CHAIN_ID)
                .signature(createSignature())
                .link(createLink())
                .build());
        linkMetaBlockRepository.save(LinkMetaBlock
                .builder()
                .supplyChainId(SUPPLY_CHAIN_ID)
                .signature(createSignature())
                .link(createLink())
                .build());
        linkMetaBlockRepository.save(LinkMetaBlock
                .builder()
                .supplyChainId(OTHER_SUPPLY_CHAIN_ID)
                .signature(createSignature())
                .link(createLink())
                .build());
    }
    
    private Signature createSignature() {
        return Signature.builder()
                .keyId("2392017103413adf6fa3b535e3714b30bc0a901229d0e76784f5ffca653f905e")
                .signature("signature")
                .build();
    }

    private Link createLink() {
        return Link
                .builder()
                .stepName(STEP_NAME)
                .materials(createMaterials())
                .products(createProducts())
                .build();
    }

    private List<Artifact> createMaterials() {
        return asList(

                Artifact.builder()
                        .hash(HASH_1)
                        .uri(ARGOS_TEST_IML)
                        .build(),

                Artifact.builder()
                        .hash(HASH_2)
                        .uri(DOCKER_1_IML)
                        .build());
    }

    private List<Artifact> createProducts() {
        return asList(

                Artifact.builder()
                        .hash(HASH_1)
                        .uri(ARGOS_TEST_IML)
                        .build(),

                Artifact.builder()
                        .hash(HASH_2)
                        .uri(DOCKER_1_IML)
                        .build());
    }
}