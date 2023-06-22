package com.argosnotary.argos.service.itest.mongodb;

import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

public class ArgosTestContainers {

	private static final String MONGO_IMAGE_NAME = "mongo:6";
	private static final String MONGO_IMAGE_NAME_PROPERTY = "mongo.default.image.name";
	
	private static final String KEYCLOAK_IMAGE_NAME = "quay.io/keycloak/keycloak:21.1.1";

	public static MongoDBContainer getMongoDBContainer() {
		return new MongoDBContainer(DockerImageName.parse(System.getProperty(MONGO_IMAGE_NAME_PROPERTY, MONGO_IMAGE_NAME)))
				.waitingFor(Wait.forListeningPort());
	}
	
	public static GenericContainer getKeycloakContainer() {
		GenericContainer<?> genericContainer = new GenericContainer<>(KEYCLOAK_IMAGE_NAME)
				.withClasspathResourceMapping("dev/keycloak/import", "/opt/keycloak/data/import", BindMode.READ_ONLY)
		        .withExposedPorts(8080)
		        .withCommand("start-dev -Dkeycloak.migration.action=import -Dkeycloak.migration.provider=dir -Dkeycloak.migration.dir=/opt/keycloak/data/import")
		        .waitingFor(Wait.forLogMessage(".*Profile dev activated.*\\n", 1));
		return genericContainer;
	}
}
