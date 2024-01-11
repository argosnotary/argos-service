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

import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

public class ArgosTestContainers {

	private static final String MONGO_IMAGE_NAME = "mongo:6";
	private static final String MONGO_IMAGE_NAME_PROPERTY = "mongo.default.image.name";
	
	private static final String KEYCLOAK_IMAGE_NAME = "quay.io/keycloak/keycloak:23.0";

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
