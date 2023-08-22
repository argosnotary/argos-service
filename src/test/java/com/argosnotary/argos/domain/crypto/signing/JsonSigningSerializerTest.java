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
package com.argosnotary.argos.domain.crypto.signing;

import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.argosnotary.argos.domain.attest.ArgosDigest;
import com.argosnotary.argos.domain.attest.Attestation;
import com.argosnotary.argos.domain.attest.AttestationData;
import com.argosnotary.argos.domain.attest.Digest;
import com.argosnotary.argos.domain.attest.Predicate;
import com.argosnotary.argos.domain.attest.ResourceDescriptor;
import com.argosnotary.argos.domain.attest.Statement;
import com.argosnotary.argos.domain.attest.predicate.provenance.BuildDefinition;
import com.argosnotary.argos.domain.attest.predicate.provenance.Builder;
import com.argosnotary.argos.domain.attest.predicate.provenance.Metadata;
import com.argosnotary.argos.domain.attest.predicate.provenance.Provenance;
import com.argosnotary.argos.domain.attest.predicate.provenance.RunDetails;
import com.argosnotary.argos.domain.attest.statement.InTotoStatement;
import com.argosnotary.argos.domain.crypto.PublicKey;
import com.argosnotary.argos.domain.layout.ArtifactType;
import com.argosnotary.argos.domain.layout.Layout;
import com.argosnotary.argos.domain.layout.Step;
import com.argosnotary.argos.domain.layout.rule.MatchRule;
import com.argosnotary.argos.domain.layout.rule.Rule;
import com.argosnotary.argos.domain.layout.rule.RuleType;
import com.argosnotary.argos.domain.link.Artifact;
import com.argosnotary.argos.domain.link.Link;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

class JsonSigningSerializerTest {

    private final static String PUBLIC_KEY = "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEjdjAZjwvCrYGEv/zKVAhSItOV91OpPGmMPNCR3Dr0oryke0PhRO6HCbb+kS5NOJUEaGHbFeJUujpn/zQQIVlkQ==";
    private UUID SUPPLYCHAIN_ID = UUID.fromString("76496480-d641-4770-a2c0-766bc588afd1");

    @Test
    void serializeLink() throws IOException {
        String serialized = new JsonSigningSerializer().serialize(Link.builder()
                .stepName("stepName")
                .materials(Arrays.asList(
                        Artifact.builder().uri("zbc.jar").hash("hash1").build(),
                        Artifact.builder().uri("abc.jar").hash("hash2").build()))
                .products(Arrays.asList(
                        Artifact.builder().uri("_bc.jar").hash("hash3").build(),
                        Artifact.builder().uri("_abc.jar").hash("hash4").build()))
                .build());
        String expectedJson = getExpectedJson("/expectedLinkSigning.json");
        assertThat(serialized, is(expectedJson));
    }

    @Test
    void serializeLayout() throws IOException, GeneralSecurityException {
    	
    	Layout layout = Layout.builder()
                .keys(Arrays.asList(new PublicKey("keyId", Base64.getDecoder().decode(PUBLIC_KEY))))
                .expectedEndProducts(singletonList(MatchRule.builder()
                        .destinationType(ArtifactType.PRODUCTS)
                        .destinationStepName("destinationStepName")
                        .pattern("MatchFiler").build()))
                .steps(Arrays.asList(
                        Step.builder()
                            .name("stepb")
                            .requiredNumberOfLinks(1)
                            .expectedMaterials(
                                    Arrays.asList(
                                            new Rule(RuleType.ALLOW, "AllowRule"),
                                            new Rule(RuleType.REQUIRE, "RequireRule")
                                    ))
                            .expectedProducts(Arrays.asList(
                                    new Rule(RuleType.CREATE, "CreateRule"),
                                    new Rule(RuleType.MODIFY, "ModifyRule")
                                    ))
                            .build(),
                        Step.builder()
                            .name("stepa")
                            .authorizedKeyIds(Arrays.asList("step a key 2", "step a key 1"))
                            .requiredNumberOfLinks(23)
                            .expectedProducts(Arrays.asList(
                                    new Rule(RuleType.DISALLOW, "DisAllowRule"),
                                    MatchRule.builder().pattern("MatchRule")
                                        .destinationPathPrefix("destinationPathPrefix")
                                        .sourcePathPrefix("sourcePathPrefix")
                                        .destinationStepName("destinationStepName")
                                        .destinationType(ArtifactType.MATERIALS)
                                        .build(),
                                    new Rule(RuleType.DELETE, "DeleteRule")
                                    ))
                        .build()
                        ))
                .authorizedKeyIds(Arrays.asList("key2", "key1"))
                .build();
        String serialized = new JsonSigningSerializer().serialize(layout);
        String expectedJson = getExpectedJson("/expectedLayoutSigning.json");
        assertThat(serialized, is(expectedJson));
    }

    private String getExpectedJson(String name) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readValue(this.getClass().getResource(name), JsonNode.class);
        return jsonNode.toString();
    }

	@Test
	void testPredicateJsonSerialize() throws URISyntaxException {
		Statement st = (Statement) AttestationData.createTestData().get("at2").getEnvelope().getPayload();
		String serialized = new JsonSigningSerializer().serialize(st);
		assertEquals("{\"_type\":\"https://in-toto.io/Statement/v1\",\"predicate\":{\"buildDefinition\":{\"resolvedDependencies\":[{\"argosDigest\":{\"hash\":\"hash2\"},\"uri\":\"uri2\"}]},\"runDetails\":{\"builder\":{\"builderDependencies\":[{\"argosDigest\":{\"hash\":\"86b64f3da76f56e46f800a80945ac8fdf67719e4\"},\"uri\":\"https://github.com/argosnotary/argos-service/commit/86b64f3da76f56e46f800a80945ac8fdf67719e4\"}],\"version\":{}},\"metadata\":{\"finishedOn\":\"1985-04-12T23:25:50.52Z\",\"invocationId\":\"theInvocationId\",\"startedOn\":\"1985-04-12T23:20:50.52Z\"}}},\"predicateType\":\"https://slsa.dev/provenance/v1\",\"subject\":[{\"argosDigest\":{\"hash\":\"hash2\"},\"uri\":\"uri2\"}]}", serialized);
	}
}
