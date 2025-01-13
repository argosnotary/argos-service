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

import static com.argosnotary.argos.service.rest.ValidateHelper.expectedErrors;
import static com.argosnotary.argos.service.rest.ValidateHelper.validate;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.argosnotary.argos.domain.attest.Attestation;
import com.argosnotary.argos.domain.attest.AttestationData;
import com.argosnotary.argos.service.openapi.rest.model.RestAttestation;
import com.argosnotary.argos.service.openapi.rest.model.RestBuildDefinition;
import com.argosnotary.argos.service.openapi.rest.model.RestEnvelope;
import com.argosnotary.argos.service.openapi.rest.model.RestInTotoStatement;
import com.argosnotary.argos.service.openapi.rest.model.RestProvenance;
import com.argosnotary.argos.service.openapi.rest.model.RestSignature;

class RestAttestationTest {
	
	private static final Map<String, Attestation> DATA_MAP = AttestationData.createTestData();


    @Test
    void emptyInvalidSignature() {
    	RestInTotoStatement st = new RestInTotoStatement().predicate(new RestProvenance().buildDefinition(new RestBuildDefinition())).predicateType("foo").type("theType");
    	assertThat(validate(new RestAttestation()
    			.envelope(new RestEnvelope().payload(st).signatures(List.of(new RestSignature().keyId("theKeyId").sig("theSig"))))), contains(expectedErrors(
    	        "envelope.payload.predicate.runDetails", "must not be null", 
        		"envelope.signatures[0].hashAlgorithm", "must not be null", 
        		"envelope.signatures[0].keyAlgorithm", "must not be null",
        		"envelope.signatures[0].keyId", "must match \"^[0-9a-f]*$\"",
        		"envelope.signatures[0].keyId", "size must be between 24 and 128",
        		"envelope.signatures[0].sig", "must match \"^[0-9a-f]*$\""
        )));
    }

}
