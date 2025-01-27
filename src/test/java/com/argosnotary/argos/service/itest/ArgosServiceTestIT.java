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
package com.argosnotary.argos.service.itest;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import org.apache.http.client.ClientProtocolException;
import org.bouncycastle.operator.OperatorCreationException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.EnabledIf;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.argosnotary.argos.service.ArgosTestContainers;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.intuit.karate.junit5.Karate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
  properties="spring.data.mongodb.auto-index-creation=true")
@Testcontainers
class ArgosServiceTestIT {

    private static final String DEFAULT_TESTDATA = "default-testdata";
    private static Properties properties = Properties.getInstance();
    private static MongoDbClient mongoClient;
	
	public static String getKeycloakUrl(String realm) {
		String url = String.format("http://%s:%s/realms/%s", 
				keycloakContainer.getHost(), 
				keycloakContainer.getFirstMappedPort(),
				realm);
		return url;
	}
	
	public static String getKeycloakAuthUrl() {
		String url = String.format("http://%s:%s",
				keycloakContainer.getHost(), 
				keycloakContainer.getFirstMappedPort());
		return url;
	}
	
	@Container //
	private static GenericContainer keycloakContainer = ArgosTestContainers.getKeycloakContainer();

	
	static MongoDBContainer mongoDBContainer = ArgosTestContainers.getMongoDBContainer();
    
    static {
        mongoDBContainer.start();
    }

	@DynamicPropertySource
	static void setProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
		//registry.add("spring.data.mongodb.auto-index-creation", () -> true);
		
		String mongo = mongoDBContainer.getConnectionString();
		
		registry.add("spring.security.oauth2.client.provider.master.issuer-uri", () -> getKeycloakUrl("master"));// String.format("http://localhost:{}/realms/master", keycloakContainerPort));
		registry.add("spring.security.oauth2.client.provider.saprovider.issuer-uri", () -> getKeycloakUrl("saprovider"));
		registry.add("spring.security.oauth2.client.provider.oauth-stub.issuer-uri", () -> getKeycloakUrl("oauth-stub"));
	}
	
	@BeforeAll
	static void setUp() throws ClientProtocolException, NoSuchAlgorithmException, OperatorCreationException, IOException {
        log.info("karate base url : {}", properties.getApiBaseUrl());
        System.setProperty(ServiceClient.SERVER_BASEURL, properties.getApiBaseUrl());
        mongoClient = new MongoDbClient(mongoDBContainer.getConnectionString());
        System.setProperty("mongo-url", mongoDBContainer.getConnectionString());
        mongoClient.resetAllRepositories();

//        ServiceClient.waitForArgosServiceToStart();
//        DefaultTestData defaultTestData = new DefaultTestData();
//        ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
//        String defaultTestDataJson = objectMapper.writeValueAsString(defaultTestData);
//        System.setProperty(DEFAULT_TESTDATA, defaultTestDataJson);
	}
	
	@BeforeEach
	void init() throws ClientProtocolException, NoSuchAlgorithmException, OperatorCreationException, IOException {
//        log.info("karate base url : {}", properties.getApiBaseUrl());
//        System.setProperty(SERVER_BASEURL, properties.getApiBaseUrl());
//        mongoClient = new MongoDbClient(mongoDBContainer.getConnectionString());
//        mongoClient.resetAllRepositories();


//        System.setProperty(ServiceClient.SERVER_BASEURL, properties.getApiBaseUrl());//+":"+serverPort);
//        log.info("karate base url : {}", System.getProperty(ServiceClient.SERVER_BASEURL));
		ServiceClient.waitForArgosServiceToStart();
		if (System.getProperty(DEFAULT_TESTDATA) == null) {
	        DefaultTestData defaultTestData = new DefaultTestData();
	        ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
	        String defaultTestDataJson = objectMapper.writeValueAsString(defaultTestData);
	        System.setProperty(DEFAULT_TESTDATA, defaultTestDataJson);
		}
	}

    @Karate.Test
    Karate personalaccount() {
        return Karate.run("classpath:feature/account/personalaccount.feature").tags("~@ignore");
    }

    @Karate.Test
    Karate serviceAccount() {
        return Karate.run("classpath:feature/account/service-account.feature").tags("~@ignore");
    }

    @Karate.Test
    Karate organization() {
        return Karate.run("classpath:feature/nodes/organization.feature").tags("~@ignore");
    }

    @Karate.Test
    Karate managementNode() {
        return Karate.run("classpath:feature/nodes/management-node.feature").tags("~@ignore");
    }

    @Karate.Test
    Karate project() {
        return Karate.run("classpath:feature/nodes/project.feature").tags("~@ignore");
    }

    @Karate.Test
    Karate supplychain() {
        return Karate.run("classpath:feature/nodes/supplychain.feature").tags("~@ignore");
    }

    @Karate.Test()
    Karate oauthProviders() {
        return Karate.run("classpath:feature/oauthprovider/oauthprovider.feature").tags("~@ignore");
    }

    @Karate.Test
    Karate roles() {
        return Karate.run("classpath:feature/roles/roles.feature").tags("~@ignore");
    }

    @Karate.Test
    Karate link() {
        return Karate.run("classpath:feature/link/link.feature").tags("~@ignore");
    }

    @Karate.Test
    Karate attestation() {
        return Karate.run("classpath:feature/attestation/attestation.feature").tags("~@ignore");
    }

    @Karate.Test
    Karate layout() {
        return Karate.run("classpath:feature/layout/layout.feature").tags("~@ignore");
    }

    @Karate.Test
    Karate verification() {
        return Karate.run("classpath:feature/verification/verification.feature").tags("~@ignore");
    }

    @Karate.Test
    Karate release() {
        return Karate.run("classpath:feature/release/release.feature").tags("~@ignore");
    }

    @Karate.Test
    Karate verification2() {
        return Karate.run("classpath:feature/verification2.0/verification2.0.feature").tags("~@ignore");
    }
}
