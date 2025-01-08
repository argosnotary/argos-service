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

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.argosnotary.argos.domain.crypto.signing.SignatureValidator;

class AttestationDataTest {
	
	private static final Map<String, Attestation> DATA_MAP = AttestationData.createTestData();

	@BeforeEach
	void setUp() throws Exception {
	}

	@Test
	void test() {
		DATA_MAP.entrySet().forEach(e -> {
			assertTrue(SignatureValidator.isValid(e.getValue().getEnvelope().getPayload(), e.getValue().getEnvelope().getSignatures().get(0), AttestationData.ecPair));
		});
	}

}
