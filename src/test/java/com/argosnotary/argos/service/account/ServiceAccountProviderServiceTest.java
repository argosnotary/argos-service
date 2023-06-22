package com.argosnotary.argos.service.account;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.argosnotary.argos.service.itest.mongodb.ArgosTestContainers;


@Testcontainers
class ServiceAccountProviderServiceTest {
	
	private static String getKeycloakUrl(String realm) {
		String url = String.format("http://%s:%s/realms/%s", 
				keycloakContainer.getHost(), 
				keycloakContainer.getFirstMappedPort(),
				realm);
		return url;
	}
	
	@Container //
	private static GenericContainer keycloakContainer = ArgosTestContainers.getKeycloakContainer();

	@DynamicPropertySource
	static void setProperties(DynamicPropertyRegistry registry) {
		//registry.add("spring.security.oauth2.client.provider.master.issuer-uri", () -> getKeycloakUrl("master"));// String.format("http://localhost:{}/realms/master", keycloakContainerPort));
		registry.add("spring.security.oauth2.client.provider.saprovider.issuer-uri", () -> getKeycloakUrl("saprovider"));
		//registry.add("spring.security.oauth2.client.provider.oauth-stub.issuer-uri", () -> getKeycloakUrl("oauth-stub"));
	}

	@BeforeEach
	void setUp() throws Exception {
	}

	@Test
	void test() {
		//fail("Not yet implemented");
	}

}
