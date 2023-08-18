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
package com.argosnotary.argos.service.mongodb.release;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.EnabledIf;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.argosnotary.argos.domain.account.PersonalAccount;
import com.argosnotary.argos.domain.layout.LayoutMetaBlock;
import com.argosnotary.argos.domain.link.LinkMetaBlock;
import com.argosnotary.argos.domain.nodes.Domain;
import com.argosnotary.argos.domain.nodes.Organization;
import com.argosnotary.argos.domain.release.Release;
import com.argosnotary.argos.domain.release.ReleaseDossier;
import com.argosnotary.argos.service.ArgosTestContainers;
import com.argosnotary.argos.service.JsonMapperConfig;
import com.argosnotary.argos.service.mongodb.MongoConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.gridfs.model.GridFSFile;

@Testcontainers
@DataMongoTest(properties= {"spring.data.mongodb.auto-index-creation=true","spring.data.mongodb.database=argos"})
@ImportAutoConfiguration(classes= {MongoConfig.class, ReleaseDossierRepository.class, JsonMapperConfig.class})
class ReleaseDossierRepositoryTest {

	static MongoDBContainer mongoDBContainer = ArgosTestContainers.getMongoDBContainer();
    
    static {
        mongoDBContainer.start();
    }

	@DynamicPropertySource
	static void setProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
	}
	
	private Release r11;
	private Organization org1;

	@Autowired
	ReleaseDossierRepository releaseDossierRepository;
	
//	@Autowired
//	private GridFsTemplate gridFsTemplate;
	
	JsonMapperConfig jsonMapperConfig = new JsonMapperConfig();
	
	private ObjectMapper mapper = jsonMapperConfig.objectMapper();
	
	@Mock
	private LayoutMetaBlock layoutMetaBlock;
	
	@Mock
	private LinkMetaBlock linkMetaBlock;
	
	@Mock
	private PersonalAccount pa;

	@BeforeEach
	void setUp() throws Exception {
		org1 = new Organization(UUID.randomUUID(), "org1", Domain.builder().name("domainName1").build());
		
		r11 = Release.builder()
				.name("releaseName11")
				.qualifiedSupplyChainName("supply1.domainName1")
				.releaseDate(OffsetDateTime.now(ZoneOffset.UTC))
				.supplyChainId(UUID.randomUUID())
				.id(UUID.randomUUID())
				.domain(Domain.builder()
						.name("domainName1")
						.build())
				.releasedProductsHashes(Set.of("hash111","hash112","hash113")).build();
		
		
	}

	@Test
	void testStoreRelease() {

        ReleaseDossier releaseDossier = ReleaseDossier.builder()
                .layoutMetaBlock(layoutMetaBlock)
                .linkMetaBlocks(List.of(linkMetaBlock))
                .accounts(List.of(pa))
                .build();
		
		Release r = releaseDossierRepository.storeRelease(r11, releaseDossier);
		assertNotNull(r.getDossierId());
		GridFSFile f = releaseDossierRepository.findByFileId(r.getDossierId());
		assertThat(f.getFilename(), is(r11.getName()+".json"));
		
	}

}
