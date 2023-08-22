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
package com.argosnotary.argos.service.mongodb.attest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.argosnotary.argos.domain.attest.ArgosDigest;
import com.argosnotary.argos.domain.attest.Attestation;
import com.argosnotary.argos.domain.attest.AttestationData;
import com.argosnotary.argos.domain.attest.Envelope;
import com.argosnotary.argos.domain.attest.ResourceDescriptor;
import com.argosnotary.argos.domain.attest.Statement;
import com.argosnotary.argos.domain.attest.predicate.provenance.BuildDefinition;
import com.argosnotary.argos.domain.attest.predicate.provenance.Builder;
import com.argosnotary.argos.domain.attest.predicate.provenance.Metadata;
import com.argosnotary.argos.domain.attest.predicate.provenance.Provenance;
import com.argosnotary.argos.domain.attest.predicate.provenance.RunDetails;
import com.argosnotary.argos.domain.attest.statement.InTotoStatement;
import com.argosnotary.argos.domain.crypto.Signature;
import com.argosnotary.argos.service.ArgosTestContainers;
import com.argosnotary.argos.service.mongodb.MongoConfig;

@Testcontainers
@DataMongoTest
@ImportAutoConfiguration(classes=MongoConfig.class)
class AttestationRepositoryTest {
	
	private static final Map<String, Attestation> DATA_MAP = AttestationData.createTestData();

    private static final UUID SUPPLY_CHAIN_ID = DATA_MAP.get("at1").getSupplyChainId();
	
	static MongoDBContainer mongoDBContainer = ArgosTestContainers.getMongoDBContainer();
    
    static {
        mongoDBContainer.start();
    }

	@DynamicPropertySource
	static void setProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
	}

	@Autowired 
	AttestationRepository attestationRepository;
	
	@Autowired
	ApplicationContext applicationContext;
	
    private static final String HASH_1 = "hash1";
    
    private Attestation at1, at2, at3, at4;
	
    @BeforeEach
    void setup() throws IOException, ClassNotFoundException, URISyntaxException {
    	at1 = DATA_MAP.get("at1");
    	at2 = DATA_MAP.get("at2");
    	at3 = DATA_MAP.get("at3");
    	at4 = DATA_MAP.get("at4");
    	attestationRepository.deleteAll();
        createDataSet();
    }
    
    @Test
    void findAll() {
        List<Attestation> ats = attestationRepository.findAll();
        assertThat(ats, hasSize(4));
    }
    
    @Test
    void findBySupplyChainId() {
        List<Attestation> ats = attestationRepository.findBySupplyChainId(SUPPLY_CHAIN_ID);
        assertThat(ats, hasSize(3));
    }
    
    @Test
    void findBySupplyChainIdAndHash() {
        List<Attestation> ats = attestationRepository.findBySupplyChainIdAndHash(SUPPLY_CHAIN_ID, HASH_1);
        assertThat(ats, hasSize(1));
        assertEquals(at1, ats.get(0));
    }
    
    @Test
    void deleteBySupplyChainId() {
        attestationRepository.deleteBySupplyChainId(SUPPLY_CHAIN_ID);

        List<Attestation> ats = attestationRepository.findAll();
        assertThat(ats, hasSize(1));
    }
    

    void createDataSet() {
    	attestationRepository.save(at1);
    	attestationRepository.save(at2);
    	attestationRepository.save(at3);
    	attestationRepository.save(at4);
    }
    
    private Signature createSignature(String suffix) {
        return Signature.builder()
                .keyId("2392017103413adf6fa3b535e3714b30bc0a901229d0e76784f5ffca653f905e")
                .sig("signature"+suffix)
                .build();
    }
}