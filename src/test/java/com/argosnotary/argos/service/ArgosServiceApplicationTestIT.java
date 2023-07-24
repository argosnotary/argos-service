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
package com.argosnotary.argos.service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.argosnotary.argos.service.itest.mongodb.ArgosTestContainers;


@SpringBootTest
@Testcontainers
class ArgosServiceApplicationTestIT {
	
	private static String getKeycloakUrl(String realm) {
		String url = String.format("http://%s:%s/realms/%s", 
				keycloakContainer.getHost(), 
				keycloakContainer.getFirstMappedPort(),
				realm);
		return url;
	}
	
	@Container //
	private static GenericContainer keycloakContainer = ArgosTestContainers.getKeycloakContainer();

	
	@Container //
	private static MongoDBContainer mongoDBContainer = ArgosTestContainers.getMongoDBContainer();

	@DynamicPropertySource
	static void setProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
		
		registry.add("spring.security.oauth2.client.provider.master.issuer-uri", () -> getKeycloakUrl("master"));// String.format("http://localhost:{}/realms/master", keycloakContainerPort));
		registry.add("spring.security.oauth2.client.provider.saprovider.issuer-uri", () -> getKeycloakUrl("saprovider"));
		registry.add("spring.security.oauth2.client.provider.oauth-stub.issuer-uri", () -> getKeycloakUrl("oauth-stub"));
	}

	@Test
	void contextLoads() throws InterruptedException {
		System.out.println("In contextLoads");
	}

}
