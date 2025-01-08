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
package com.argosnotary.argos.domain.attest.predicate.provenance;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.argosnotary.argos.domain.attest.ArgosDigest;
import com.argosnotary.argos.domain.attest.ResourceDescriptor;

class ProvenanceTest {
	static final  String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SS'Z'";

	@BeforeEach
	void setUp() throws Exception {
	}

	@Test
	void testGetPredicateType() throws MalformedURLException {
		Provenance p = Provenance.builder().build();
		assertEquals(new URL("https://slsa.dev/provenance/v1"), p.getPredicateType());
	}

	@Test
	void testCloneCanonical() throws MalformedURLException, URISyntaxException {
		ResourceDescriptor r1 = ResourceDescriptor.builder().digest(Map.of()).argosDigest(ArgosDigest.builder().hash("hash1").build()).uri(URI.create("uri1")).build();
        ResourceDescriptor r2 = ResourceDescriptor.builder().digest(Map.of()).argosDigest(ArgosDigest.builder().hash("hash2").build()).uri(URI.create("uri2")).build();
		
		ResourceDescriptor gitCommit = ResourceDescriptor.builder().digest(Map.of())
					.uri(new URI("https://github.com/argosnotary/argos-service/commit/86b64f3da76f56e46f800a80945ac8fdf67719e4")).argosDigest(ArgosDigest.builder().hash("86b64f3da76f56e46f800a80945ac8fdf67719e4").build()).build();
		
		Builder b = Builder.builder().version(Map.of("key2", "value", "key1", "value")).builderDependencies(List.of(r1, gitCommit)).build();
		LocalDateTime startedOn = LocalDateTime.parse("1985-04-12T23:20:50.52Z", DateTimeFormatter.ofPattern(DATE_FORMAT));
		LocalDateTime finishedOn = LocalDateTime.parse("1985-04-12T23:25:50.52Z", DateTimeFormatter.ofPattern(DATE_FORMAT));
		Metadata m = Metadata.builder()
				.invocationId("theInvocationId")
				.startedOn(OffsetDateTime.of(startedOn, ZoneOffset.UTC))
				.finishedOn(OffsetDateTime.of(finishedOn, ZoneOffset.UTC))
				.build(); 
		Provenance p = Provenance.builder().buildDefinition(BuildDefinition.builder().internalParameters(Map.of("key2", "value", "key1", "value")).externalParameters(Map.of("key2", "value", "key1", "value")).resolvedDependencies(List.of(r2,r1)).build()).runDetails(RunDetails.builder().builder(b).metadata(m).build()).build();
		
		Builder expectedB = Builder.builder().version(Map.of("key1", "value", "key2", "value")).builderDependencies(List.of(gitCommit, r1)).build();
		Provenance expected = Provenance.builder().buildDefinition(BuildDefinition.builder().internalParameters(Map.of("key1", "value", "key2", "value")).externalParameters(Map.of("key1", "value", "key2", "value")).resolvedDependencies(List.of(r1,r2)).build()).runDetails(RunDetails.builder().builder(expectedB).metadata(m).build()).build();
		
		assertEquals(expected, p.cloneCanonical());
	}

	@Test
	void testCloneCanonicalNulls() throws MalformedURLException {
		Provenance p = Provenance.builder().build();
		assertEquals(p, p.cloneCanonical());
	}
	
	

}
