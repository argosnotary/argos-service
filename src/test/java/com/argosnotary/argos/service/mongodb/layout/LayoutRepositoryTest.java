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
package com.argosnotary.argos.service.mongodb.layout;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
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
import com.argosnotary.argos.domain.layout.Layout;
import com.argosnotary.argos.domain.layout.LayoutMetaBlock;
import com.argosnotary.argos.service.ArgosTestContainers;


@Testcontainers
@DataMongoTest
class LayoutRepositoryTest {
	
	static MongoDBContainer mongoDBContainer = ArgosTestContainers.getMongoDBContainer();
    
    static {
        mongoDBContainer.start();
    }
    
	@DynamicPropertySource
	static void setProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
	}

	@Autowired LayoutMetaBlockRepository layoutMetaBlockRepository;
	
	private static final UUID SUPPLY_CHAIN_ID = UUID.randomUUID();
    private static final UUID OTHER_SUPPLY_CHAIN_ID = UUID.randomUUID();
    private Layout l1, l2, l3, l4;
    
    private LayoutMetaBlock la1, la2, la3, la4;
	
    @BeforeEach
    void setup() throws IOException, ClassNotFoundException {
    	l1 = Layout.builder().build();
    	l2 = Layout.builder().build();
    	l3 = Layout.builder().build();
    	l4 = Layout.builder().build();
    	la1 = LayoutMetaBlock
                .builder()
                .supplyChainId(SUPPLY_CHAIN_ID)
                .signatures(List.of(createSignature()))
                .layout(l1)
                .build();
        la2 = LayoutMetaBlock
                .builder()
                .supplyChainId(SUPPLY_CHAIN_ID)
                .signatures(List.of(createSignature()))
                .layout(l2)
                .build();
        la3 = LayoutMetaBlock
                .builder()
                .supplyChainId(SUPPLY_CHAIN_ID)
                .signatures(List.of(createSignature()))
                .layout(l3)
                .build();
        la4 = LayoutMetaBlock
                .builder()
                .supplyChainId(OTHER_SUPPLY_CHAIN_ID)
                .signatures(List.of(createSignature()))
                .layout(l4)
                .build();
    	layoutMetaBlockRepository.deleteAll();
        createDataSet();
    }
    
    @Test
    void findBySupplyChainId() {
        Optional<LayoutMetaBlock> block = layoutMetaBlockRepository.findById(SUPPLY_CHAIN_ID);
        assertTrue(block.isPresent());
        List<LayoutMetaBlock> blocks = layoutMetaBlockRepository.findBySupplyChainId(SUPPLY_CHAIN_ID);
        assertThat(blocks, hasSize(1));
        block = layoutMetaBlockRepository.findById(UUID.randomUUID());
        assertFalse(block.isPresent());
    }
    
    @Test
    void deleteBySupplyChainId() {
    	layoutMetaBlockRepository.deleteBySupplyChainId(SUPPLY_CHAIN_ID);

        List<LayoutMetaBlock> blocks = layoutMetaBlockRepository.findAll();
        assertThat(blocks, hasSize(1));
    }
    

    void createDataSet() {
    	la1 = layoutMetaBlockRepository.save(la1);
    	layoutMetaBlockRepository.save(la2);
    	layoutMetaBlockRepository.save(la3);
    	layoutMetaBlockRepository.save(la4);
    }
    
    private Signature createSignature() {
        return Signature.builder()
                .keyId("2392017103413adf6fa3b535e3714b30bc0a901229d0e76784f5ffca653f905e")
                .sig("signature")
                .build();
    }
}