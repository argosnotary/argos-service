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
package com.argosnotary.argos.domain.attest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.argosnotary.argos.domain.ArgosError;
import com.argosnotary.argos.domain.attest.statement.InTotoStatement;
import com.argosnotary.argos.domain.crypto.HashAlgorithm;
import com.argosnotary.argos.domain.attest.ResourceDescriptor.ResourceDescriptorBuilder;

class ResourceDescriptorTest {
	ResourceDescriptor r1, r2;
	
	private static final Map<String, Attestation> DATA_MAP = AttestationData.createTestData();

	@BeforeEach
	void setUp() throws Exception {
		r1 = ((InTotoStatement)DATA_MAP.get("at1").getEnvelope().getPayload()).getSubject().get(0);
		r2 = ((InTotoStatement)DATA_MAP.get("at2").getEnvelope().getPayload()).getSubject().get(0);
	}

	@Test
	void testCompareTo() throws URISyntaxException {
		ResourceDescriptor t = ((InTotoStatement)DATA_MAP.get("at1").getEnvelope().getPayload()).getSubject().get(0);
		assertEquals(0, r1.compareTo(t));
		assertEquals(1, r2.compareTo(r1));
		assertEquals(-1, r1.compareTo(r2));
		
		ResourceDescriptor gitCommit = ResourceDescriptor.builder().digest(Map.of())
					.uri(new URI("https://github.com/argosnotary/argos-service/commit/86b64f3da76f56e46f800a80945ac8fdf67719e4")).argosDigest(ArgosDigest.builder().hash("86b64f3da76f56e46f800a80945ac8fdf67719e4").build()).build();

		assertEquals(-1, r1.compareTo(gitCommit));
		
	}

	@Test
	void testCloneCanonical() {
		ResourceDescriptor r2 = ResourceDescriptor.builder().digest(Map.of("key2", "value", "key1", "value")).argosDigest(ArgosDigest.builder().hash("hash2").build()).uri(URI.create("uri2")).build();
		ResourceDescriptor expected = ResourceDescriptor.builder().digest(Map.of("key1", "value", "key2", "value")).argosDigest(ArgosDigest.builder().hash("hash2").build()).uri(URI.create("uri2")).build();
		assertEquals(expected, r2.cloneCanonical());
	}
	
	@Test
	void testConstructorNulls() {
		ResourceDescriptorBuilder b1 = ResourceDescriptor.builder();
		Throwable exception = assertThrows(ArgosError.class, () -> {
			b1.build();
          });
        
        assertEquals("Wrong ResourceDescriptor defintion, properties are null: uri: [null], argosDigest: [null]", exception.getMessage());
        
		ResourceDescriptorBuilder b2 = ResourceDescriptor.builder();
		
        exception = assertThrows(ArgosError.class, () -> {
			b2.uri(URI.create("uri")).build();
          });
        
        assertEquals("Wrong ResourceDescriptor defintion, properties are null: uri: [uri], argosDigest: [null]", exception.getMessage());
        
		ResourceDescriptorBuilder b3 = ResourceDescriptor.builder();
		
        exception = assertThrows(ArgosError.class, () -> {
			b3.argosDigest(ArgosDigest.builder().build()).build();
          });
        
        assertEquals("Wrong ResourceDescriptor defintion, properties are null: uri: [null], argosDigest: [ArgosDigest(hash=null, algorithm=null)]", exception.getMessage());
	}
	
	@Test
	void testCloneCanonicalNulls() {
		ResourceDescriptor r2 = ResourceDescriptor.builder().digest(null).argosDigest(ArgosDigest.builder().hash("hash2").build()).uri(URI.create("uri2")).build();
		assertEquals(r2, r2.cloneCanonical());
		r2 = ResourceDescriptor.builder().digest(Map.of("key2","value", "key1", "value")).argosDigest(ArgosDigest.builder().algorithm(HashAlgorithm.SHA256).hash("hash2").build()).uri(URI.create("uri2")).build();

		ResourceDescriptor expected = ResourceDescriptor.builder().digest(Map.of("key1","value", "key2", "value")).argosDigest(ArgosDigest.builder().algorithm(HashAlgorithm.SHA256).hash("hash2").build()).uri(URI.create("uri2")).build();
		assertEquals(expected, r2.cloneCanonical());
	}

}
