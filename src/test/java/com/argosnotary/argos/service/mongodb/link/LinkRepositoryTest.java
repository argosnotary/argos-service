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
package com.argosnotary.argos.service.mongodb.link;

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
import org.springframework.test.context.junit.jupiter.EnabledIf;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.argosnotary.argos.domain.crypto.Signature;
import com.argosnotary.argos.domain.link.Artifact;
import com.argosnotary.argos.domain.link.Link;
import com.argosnotary.argos.domain.link.LinkMetaBlock;
import com.argosnotary.argos.service.ArgosTestContainers;


@Testcontainers
@DataMongoTest
class LinkRepositoryTest {
	
	static MongoDBContainer mongoDBContainer = ArgosTestContainers.getMongoDBContainer();
    
    static {
        mongoDBContainer.start();
    }

	@DynamicPropertySource
	static void setProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
	}

	@Autowired LinkMetaBlockRepository linkMetaBlockRepository;
	
	private static final String STEP_NAME = "stepName";
    private static final String STEP_NAME_NEW = "stepNameNew";
    private static final UUID SUPPLY_CHAIN_ID = UUID.randomUUID();
    private static final UUID OTHER_SUPPLY_CHAIN_ID = UUID.randomUUID();
    private static final String HASH_1 = "hash1";
    private static final String HASH_2 = "hash2";
    private static final String HASH_3 = "hash3";
    private static final String HASH_4 = "hash4";
    private static final String uri1 = "uri1";
    private static final String uri2 = "uri2";
    private static final String uri3 = "uri3";
    private static final String uri4 = "uri4";
    private Artifact a1, a2, a3, a4;
    private Link l1, l2, l3, l4;
    
    private LinkMetaBlock lb1, lb2, lb3, lb4;
	
    @BeforeEach
    void setup() throws IOException, ClassNotFoundException {
    	a1 = Artifact.builder().hash(HASH_1).uri(uri1).build();
    	a2 = Artifact.builder().hash(HASH_2).uri(uri2).build();
    	a3 = Artifact.builder().hash(HASH_3).uri(uri3).build();
    	a4 = Artifact.builder().hash(HASH_4).uri(uri4).build();
    	l1 = Link.builder().stepName(STEP_NAME).materials(List.of(a1)).products(List.of(a1)).build();
    	l2 = Link.builder().stepName(STEP_NAME).materials(List.of(a1)).products(List.of(a2)).build();
    	l3 = Link.builder().stepName(STEP_NAME).materials(List.of(a3)).products(List.of(a4)).build();
    	l4 = Link.builder().stepName(STEP_NAME).materials(List.of(a4)).products(List.of(a2)).build();
    	lb1 = LinkMetaBlock
                .builder()
                .supplyChainId(SUPPLY_CHAIN_ID)
                .signature(createSignature())
                .link(l1)
                .build();
        lb2 = LinkMetaBlock
                .builder()
                .supplyChainId(SUPPLY_CHAIN_ID)
                .signature(createSignature())
                .link(l2)
                .build();
        lb3 = LinkMetaBlock
                .builder()
                .supplyChainId(SUPPLY_CHAIN_ID)
                .signature(createSignature())
                .link(l3)
                .build();
        lb4 = LinkMetaBlock
                .builder()
                .supplyChainId(OTHER_SUPPLY_CHAIN_ID)
                .signature(createSignature())
                .link(l4)
                .build();
    	linkMetaBlockRepository.deleteAll();
        createDataSet();
    }
    
    @Test
    void findAll() {
        List<LinkMetaBlock> blocks = linkMetaBlockRepository.findAll();
        assertThat(blocks, hasSize(4));
    }
    
    @Test
    void findBySupplyChainId() {
        List<LinkMetaBlock> blocks = linkMetaBlockRepository.findBySupplyChainId(SUPPLY_CHAIN_ID);
        assertThat(blocks, hasSize(3));
    }
    
    @Test
    void findBySupplyChainIdAndHash() {
        List<LinkMetaBlock> blocks = linkMetaBlockRepository.findBySupplyChainIdAndHash(SUPPLY_CHAIN_ID, HASH_2);
        assertThat(blocks, hasSize(1));
    }
    
    @Test
    void deleteBySupplyChainId() {
        linkMetaBlockRepository.deleteBySupplyChainId(SUPPLY_CHAIN_ID);

        List<LinkMetaBlock> blocks = linkMetaBlockRepository.findAll();
        assertThat(blocks, hasSize(1));
    }
    

    void createDataSet() {
        linkMetaBlockRepository.save(lb1);
        linkMetaBlockRepository.save(lb2);
        linkMetaBlockRepository.save(lb3);
        linkMetaBlockRepository.save(lb4);
    }
    
    private Signature createSignature() {
        return Signature.builder()
                .keyId("2392017103413adf6fa3b535e3714b30bc0a901229d0e76784f5ffca653f905e")
                .signature("signature")
                .build();
    }
}