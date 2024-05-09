/*
 * Argos Notary - A new way to secure the Software Supply Chain
 *
 * Copyright (C) 2019 - 2020 Rabobank Nederland
 * Copyright (C) 2019 - 2024 Gerard Borst <gerard.borst@argosnotary.com>
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
package com.argosnotary.argos.domain.attest.predicate.provenance;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.argosnotary.argos.domain.attest.ArgosDigest;
import com.argosnotary.argos.domain.attest.ResourceDescriptor;

class BuildDefinitionTest {

	@BeforeEach
	void setUp() throws Exception {
	}

	@Test
	void testCloneCanonical() {
		BuildDefinition bd = BuildDefinition.builder().build();
		BuildDefinition bdClone = bd.cloneCanonical();
		assertEquals(bd, bdClone);

		bd = BuildDefinition.builder()
				.resolvedDependencies(List.of(
						ResourceDescriptor.builder().uri(URI.create("uri3")).argosDigest(ArgosDigest.builder().hash("hash3").build()).build(),
						ResourceDescriptor.builder().uri(URI.create("uri3")).argosDigest(ArgosDigest.builder().hash("hash1").build()).build(),
						ResourceDescriptor.builder().uri(URI.create("uri2")).argosDigest(ArgosDigest.builder().hash("hash2").build()).build(),
						ResourceDescriptor.builder().uri(URI.create("uri1")).argosDigest(ArgosDigest.builder().hash("hash1").build()).build()
						))
				.internalParameters(Map.of("key2", "value", "key1", "value"))
				.externalParameters(Map.of("key2", "value", "key1", "value")).build();

		BuildDefinition expected = BuildDefinition.builder()
				.resolvedDependencies(List.of(
						ResourceDescriptor.builder().uri(URI.create("uri1")).argosDigest(ArgosDigest.builder().hash("hash1").build()).build(),
						ResourceDescriptor.builder().uri(URI.create("uri2")).argosDigest(ArgosDigest.builder().hash("hash2").build()).build(),
						ResourceDescriptor.builder().uri(URI.create("uri3")).argosDigest(ArgosDigest.builder().hash("hash1").build()).build(),
						ResourceDescriptor.builder().uri(URI.create("uri3")).argosDigest(ArgosDigest.builder().hash("hash3").build()).build()
						))
				.internalParameters(Map.of("key1", "value", "key2", "value"))
				.externalParameters(Map.of("key1", "value", "key2", "value"))
				.build();
		bdClone = bd.cloneCanonical();
		assertEquals(expected, bdClone);
	}

}
