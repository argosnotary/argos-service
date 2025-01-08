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

import java.net.URI;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.argosnotary.argos.domain.attest.ArgosDigest;
import com.argosnotary.argos.domain.attest.ResourceDescriptor;

class RunDetailsTest {

	@BeforeEach
	void setUp() throws Exception {
	}

	@Test
	void testCloneCanonical() {
		RunDetails r = RunDetails.builder().build();
		assertEquals(r, r.cloneCanonical());
		
		r = RunDetails.builder().builder(Builder.builder().build()).build();
		assertEquals(r, r.cloneCanonical());

		ResourceDescriptor r1 = ResourceDescriptor.builder().digest(Map.of()).argosDigest(ArgosDigest.builder().hash("hash1").build()).uri(URI.create("uri1")).build();
        ResourceDescriptor r2 = ResourceDescriptor.builder().digest(Map.of()).argosDigest(ArgosDigest.builder().hash("hash2").build()).uri(URI.create("uri2")).build();
        Metadata m = Metadata.builder().invocationId("id").startedOn(OffsetDateTime.of(1973, 12, 18, 0, 0, 0, 0, ZoneOffset.UTC)).finishedOn(OffsetDateTime.of(1973, 12, 18, 1, 0, 0, 0, ZoneOffset.UTC)).build();
        Builder b = Builder.builder().builderDependencies(List.of(r2,r1)).id("id").build();
        
		r = RunDetails.builder().byproducts(List.of(r2, r1)).metadata(m).builder(b).build();
		

        Builder expectedB = Builder.builder().builderDependencies(List.of(r1,r2)).id("id").build();
        
        RunDetails expected = RunDetails.builder().byproducts(List.of(r1,r2)).metadata(m).builder(expectedB).build();
        assertEquals(expected, r.cloneCanonical());
	}

}
