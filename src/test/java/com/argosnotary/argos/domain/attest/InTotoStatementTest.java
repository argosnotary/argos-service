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
package com.argosnotary.argos.domain.attest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.argosnotary.argos.domain.ArgosError;
import com.argosnotary.argos.domain.attest.predicate.provenance.Provenance;
import com.argosnotary.argos.domain.attest.statement.InTotoStatement;

class InTotoStatementTest {
	
	private static final Map<String, Attestation> DATA_MAP = AttestationData.createTestData();

	@Test
	void testConstructor() throws URISyntaxException, MalformedURLException {
		InTotoStatement s = (InTotoStatement) DATA_MAP.get("at1").getEnvelope().getPayload();
		InTotoStatement ist = new InTotoStatement(s.getSubject(), s.getPredicate());
		assertEquals("https://in-toto.io/Statement/v1", ist.getType());
		assertEquals(new URL("https://slsa.dev/provenance/v1"), ist.getPredicateType());
		
	}
	
	@Test
	void testConstructorNulls() {
		Throwable exception = assertThrows(ArgosError.class, () -> {
			new InTotoStatement(null, null);
          });
        
        assertEquals("Wrong InTotoStatement defintion, properties are null: subject: [null], predicate: [null]", exception.getMessage());
		
        exception = assertThrows(ArgosError.class, () -> {
			new InTotoStatement(List.of(), null);
          });
        
        assertEquals("Wrong InTotoStatement defintion, properties are null: subject: [[]], predicate: [null]", exception.getMessage());
		
        exception = assertThrows(ArgosError.class, () -> {
			new InTotoStatement(null, Provenance.builder().build());
          });
        
        assertEquals("Wrong InTotoStatement defintion, properties are null: subject: [null], predicate: [Provenance(buildDefinition=null, runDetails=null)]", exception.getMessage());
	}
	
	@Test
	void testCloneCanonicalNulls() throws URISyntaxException, MalformedURLException {
		InTotoStatement expected = (InTotoStatement) DATA_MAP.get("at2clone").getEnvelope().getPayload();
		InTotoStatement s = (InTotoStatement) DATA_MAP.get("at2").getEnvelope().getPayload();
		assertEquals(expected, s.cloneCanonical());
		
	}

}
