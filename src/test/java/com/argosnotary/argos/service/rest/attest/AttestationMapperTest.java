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
package com.argosnotary.argos.service.rest.attest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.argosnotary.argos.domain.ArgosError;
import com.argosnotary.argos.domain.attest.Attestation;
import com.argosnotary.argos.domain.attest.AttestationData;
import com.argosnotary.argos.domain.attest.Envelope;
import com.argosnotary.argos.domain.attest.Predicate;
import com.argosnotary.argos.domain.attest.Statement;
import com.argosnotary.argos.domain.attest.statement.InTotoStatement;
import com.argosnotary.argos.service.JsonMapperConfig;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest(classes= {AttestationMapperImpl.class, JsonMapperConfig.class})
class AttestationMapperTest {
	
	private static final Map<String, Attestation> DATA_MAP = AttestationData.createTestData();
    
	@Autowired
	private AttestationMapper attestationMapper;
	@Autowired
	ObjectMapper objectMapper;
	
	private Attestation attest;

	@BeforeEach
	void setUp() throws Exception {
		attest = DATA_MAP.get("at2");
	}

	@Test
	void test() {
		Attestation a = attestationMapper.convertFromRestAttestation(attestationMapper.convertToRestAttestation(attest));
		assertEquals(attest, a);
	}
	
	@Test
	void testOffsetDateTime() {
		OffsetDateTime dt = attestationMapper.mapToOffsetDateTime("1985-04-12T23:20:50.52Z");
		String ts = attestationMapper.mapToString(dt);
		assertEquals("1985-04-12T23:20:50.52Z", ts);
	}
	

	@Test
	void testNotInTotoStatement() {
		Statement st = new StatementTester();
		Attestation at = Attestation.builder().envelope(Envelope.builder().payload(st).build()).build();
		
		ArgosError exception = assertThrows(ArgosError.class, () -> {
    		attestationMapper.convertToRestAttestation(at);
          });
        
        assertEquals("Unknown statement type: com.argosnotary.argos.service.rest.attest.AttestationMapperTest.StatementTester", exception.getMessage());
	}
	
	class StatementTester extends Statement {
		public StatementTester() {
			super("foo");
		}

		@Override
		public Statement cloneCanonical() {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
	

	@Test
	void testNotProvenance() {
		Predicate p = new PredicateTester();
		Attestation at = Attestation.builder().envelope(Envelope.builder().payload(new InTotoStatement(List.of(), p)).build()).build();
		
		ArgosError exception = assertThrows(ArgosError.class, () -> {
    		attestationMapper.convertToRestAttestation(at);
          });
        
        assertEquals("Unknown predicate type: com.argosnotary.argos.service.rest.attest.AttestationMapperTest.PredicateTester", exception.getMessage());
	}
	
	class PredicateTester extends Predicate {
		public PredicateTester() {
		}

		@Override
		public URL getPredicateType() {
			try {
				return new URL("http://bar.com");
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		public Predicate cloneCanonical() {
			// TODO Auto-generated method stub
			return null;
		}
		
	}

}
