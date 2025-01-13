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

import java.net.URI;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.util.io.pem.PemGenerationException;

import com.argosnotary.argos.domain.attest.predicate.provenance.BuildDefinition;
import com.argosnotary.argos.domain.attest.predicate.provenance.Builder;
import com.argosnotary.argos.domain.attest.predicate.provenance.Metadata;
import com.argosnotary.argos.domain.attest.predicate.provenance.Provenance;
import com.argosnotary.argos.domain.attest.predicate.provenance.RunDetails;
import com.argosnotary.argos.domain.attest.statement.InTotoStatement;
import com.argosnotary.argos.domain.crypto.CryptoHelper;
import com.argosnotary.argos.domain.crypto.KeyPair;
import com.argosnotary.argos.domain.crypto.Signature;
import com.argosnotary.argos.domain.crypto.signing.JsonSigningSerializer;
import com.argosnotary.argos.domain.nodes.SupplyChain;

public class AttestationData {
	
	private static final UUID SUPPLY_CHAIN_ID = UUID.randomUUID();
    private static final UUID OTHER_SUPPLY_CHAIN_ID = UUID.randomUUID();
    private static final String HASH_1 = "hash1";
    private static final String HASH_2 = "hash2";
    private static final String HASH_3 = "hash3";
    private static final String HASH_4 = "hash4";

    public static KeyPair ecPair;
    
    static JsonSigningSerializer serializer = new JsonSigningSerializer();
    
    static {
    	try {
			ecPair = CryptoHelper.createKeyPair("test".toCharArray());
		} catch (NoSuchAlgorithmException | OperatorCreationException | PemGenerationException e) {
			e.printStackTrace();
		}
    	
    }
	
    static final  String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SS'Z'";
	
	public static Map<String, Attestation> createTestData() {
		Map<String, Attestation> map = new HashMap<>();
		SupplyChain sc = new SupplyChain(UUID.randomUUID(), "sc", List.of(), UUID.randomUUID());
        sc.setPathToRoot(List.of(sc.getId(), sc.getParentId()));
        URI uri1 = null;
        URI uri2 = null;
        URI uri3 = null;
        URI uri4 = null;
		try {
			uri1 = new URI("uri1");
			uri2 = new URI("uri2");
			uri3 = new URI("uri3");
			uri4 = new URI("uri4");
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
        ResourceDescriptor r1 = ResourceDescriptor.builder().digest(Map.of()).argosDigest(ArgosDigest.builder().hash(HASH_1).build()).uri(uri1).build();
        ResourceDescriptor r2 = ResourceDescriptor.builder().digest(Map.of()).argosDigest(ArgosDigest.builder().hash(HASH_2).build()).uri(uri2).build();
        ResourceDescriptor r3 = ResourceDescriptor.builder().digest(Map.of()).argosDigest(ArgosDigest.builder().hash(HASH_3).build()).uri(uri3).build();
        ResourceDescriptor r4 = ResourceDescriptor.builder().digest(Map.of()).argosDigest(ArgosDigest.builder().hash(HASH_4).build()).uri(uri4).build();
    	ResourceDescriptor gitCommit = null;
		try {
			gitCommit = ResourceDescriptor.builder().digest(Map.of())
					.uri(new URI("https://github.com/argosnotary/argos-service/commit/86b64f3da76f56e46f800a80945ac8fdf67719e4")).argosDigest(ArgosDigest.builder().hash("86b64f3da76f56e46f800a80945ac8fdf67719e4").build()).build();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Builder b = Builder.builder().version(Map.of()).builderDependencies(List.of(gitCommit)).build();
		LocalDateTime startedOn = LocalDateTime.parse("1985-04-12T23:20:50.52Z", DateTimeFormatter.ofPattern(DATE_FORMAT));
		LocalDateTime finishedOn = LocalDateTime.parse("1985-04-12T23:25:50.52Z", DateTimeFormatter.ofPattern(DATE_FORMAT));
		Metadata m = Metadata.builder()
				.invocationId("theInvocationId")
				.startedOn(OffsetDateTime.of(startedOn, ZoneOffset.UTC))
				.finishedOn(OffsetDateTime.of(finishedOn, ZoneOffset.UTC))
				.build(); 
        Provenance p1 = Provenance.builder().buildDefinition(BuildDefinition.builder().internalParameters(Map.of()).externalParameters(Map.of()).resolvedDependencies(List.of(r1)).build()).build();
		Provenance p2 = Provenance.builder().buildDefinition(BuildDefinition.builder().internalParameters(Map.of()).externalParameters(Map.of()).resolvedDependencies(List.of(r2)).build()).runDetails(RunDetails.builder().builder(b).metadata(m).build()).build();
		Provenance p3 = Provenance.builder().buildDefinition(BuildDefinition.builder().internalParameters(Map.of()).externalParameters(Map.of()).resolvedDependencies(List.of(r3)).build()).build();
		Provenance p4 = Provenance.builder().buildDefinition(BuildDefinition.builder().internalParameters(Map.of()).externalParameters(Map.of()).resolvedDependencies(List.of(r4)).build()).build();
		Statement s1 = new InTotoStatement(List.of(r1),p1);
		Statement s2 = new InTotoStatement(List.of(r2, r1),p2);
		Statement s3 = new InTotoStatement(List.of(r3),p3);
		Statement s4 = new InTotoStatement(List.of(r4),p4);
		Statement s5 = new InTotoStatement(List.of(r1, r2),p2);
		map.put("at1", Attestation.builder()
    			.envelope(Envelope.builder().signatures(List.of(createSignature(s1))).payload(s1).build())
    			.supplyChainId(SUPPLY_CHAIN_ID)
    			.build());
		map.put("at2", Attestation.builder()
    			.envelope(Envelope.builder().signatures(List.of(createSignature(s2), createSignature(s2))).payload(s2).build())
    			.supplyChainId(SUPPLY_CHAIN_ID)
    			.build());
		map.put("at2clone", Attestation.builder()
    			.envelope(Envelope.builder().signatures(List.of(createSignature(s2), createSignature(s5))).payload(s5).build())
    			.supplyChainId(SUPPLY_CHAIN_ID)
    			.build());
		map.put("at3", Attestation.builder()
    			.envelope(Envelope.builder().signatures(List.of(createSignature(s3))).payload(s3).build())
    			.supplyChainId(SUPPLY_CHAIN_ID)
    			.build());
		map.put("at4", Attestation.builder()
    			.envelope(Envelope.builder().signatures(List.of(createSignature(s4))).payload(s4).build())
    			.supplyChainId(OTHER_SUPPLY_CHAIN_ID)
    			.build());
		
		return map;
	}
    
    private static Signature createSignature(Statement st) {
    	return CryptoHelper.sign(ecPair, "test".toCharArray(), serializer.serialize(st));
    }

}
