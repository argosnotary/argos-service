package com.argosnotary.argos.service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.EnabledIf;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.argosnotary.argos.service.itest.mongodb.ArgosTestContainers;


@EnabledIf(expression = "#{environment['spring.profiles.active'] == 'itest'}")
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
		
	}

}
