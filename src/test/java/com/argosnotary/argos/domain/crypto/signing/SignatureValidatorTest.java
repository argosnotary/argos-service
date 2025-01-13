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
package com.argosnotary.argos.domain.crypto.signing;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.util.io.pem.PemGenerationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.argosnotary.argos.domain.ArgosError;
import com.argosnotary.argos.domain.attest.Attestation;
import com.argosnotary.argos.domain.attest.AttestationData;
import com.argosnotary.argos.domain.attest.Envelope;
import com.argosnotary.argos.domain.attest.Statement;
import com.argosnotary.argos.domain.crypto.CryptoHelper;
import com.argosnotary.argos.domain.crypto.HashAlgorithm;
import com.argosnotary.argos.domain.crypto.KeyAlgorithm;
import com.argosnotary.argos.domain.crypto.KeyPair;
import com.argosnotary.argos.domain.crypto.PublicKey;
import com.argosnotary.argos.domain.crypto.Signature;
import com.argosnotary.argos.domain.layout.Layout;
import com.argosnotary.argos.domain.layout.LayoutMetaBlock;
import com.argosnotary.argos.domain.link.Artifact;
import com.argosnotary.argos.domain.link.Link;
import com.argosnotary.argos.domain.link.LinkMetaBlock;

class SignatureValidatorTest {
	
	private static final Map<String, Attestation> DATA_MAP = AttestationData.createTestData();

    private Link link;
    private Layout layout;
    //private KeyPair pair;
    private KeyPair ecPair;
    
    JsonSigningSerializer serializer = new JsonSigningSerializer();

    @BeforeEach
    void setUp() throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException, OperatorCreationException, PemGenerationException {
        link = Link.builder()
                .products(singletonList(Artifact.builder().hash("hash2").uri("/path/tofile2").build()))
                .materials(singletonList(Artifact.builder().hash("hash").uri("/path/tofile").build())).build();

        ecPair = CryptoHelper.createKeyPair("test".toCharArray());

        //ecPair = generator.generateKeyPair();
        
        layout = Layout.builder().steps(new ArrayList<>()).build();
        
    }

    @Test
    void isValid() throws InvalidKeyException, SignatureException, NoSuchAlgorithmException {
    	Signature signature = CryptoHelper.sign(ecPair, "test".toCharArray(), serializer.serialize(link));

        LinkMetaBlock linkMetaBlock = LinkMetaBlock.builder()
                .signature(signature)
                .link(link).build();
        assertThat(SignatureValidator.isValid(linkMetaBlock.getLink(), linkMetaBlock.getSignature(), (PublicKey) ecPair), is(true));

        Signature sigLayout = CryptoHelper.sign(ecPair, "test".toCharArray(), serializer.serialize(layout));
        
        List<Signature> signatures = List.of(sigLayout);
        LayoutMetaBlock layoutMetaBlock = LayoutMetaBlock.builder()
                .signatures(signatures)
                .layout(layout)
                .build();
        assertThat(SignatureValidator.isValid(layoutMetaBlock.getLayout(), layoutMetaBlock.getSignatures().get(0), (PublicKey) ecPair), is(true));
    }
    
    @Test
    void isValidEC() throws InvalidKeyException, SignatureException, NoSuchAlgorithmException {
    	Signature signature = CryptoHelper.sign(ecPair, "test".toCharArray(), serializer.serialize(link));

        LinkMetaBlock linkMetaBlock = LinkMetaBlock.builder()
                .signature(signature)
                .link(link).build();
        assertThat(SignatureValidator.isValid(linkMetaBlock.getLink(), linkMetaBlock.getSignature(), ecPair), is(true));
    }

    @Test
    void isNotValid() throws InvalidKeyException, SignatureException, NoSuchAlgorithmException {
    	Signature signature = CryptoHelper.sign(ecPair, "test".toCharArray(), serializer.serialize(link));

        link.setStepName("extra");

        LinkMetaBlock linkMetaBlock = LinkMetaBlock.builder()
                .signature(signature)
                .link(link).build();
        assertThat(SignatureValidator.isValid(linkMetaBlock.getLink(), linkMetaBlock.getSignature(), ecPair), is(false));

        Signature sigLayout = CryptoHelper.sign(ecPair, "test".toCharArray(), serializer.serialize(layout));
        
        layout.setKeys(new ArrayList<>());
        
        List<Signature> signatures = List.of(sigLayout);
        LayoutMetaBlock layoutMetaBlock = LayoutMetaBlock.builder()
                .signatures(signatures)
                .layout(layout)
                .build();
        assertThat(SignatureValidator.isValid(layoutMetaBlock.getLayout(), layoutMetaBlock.getSignatures().get(0), ecPair), is(false));
    }

    @Test
    void inValidSignature() {
        String signature = "bla";
        LinkMetaBlock linkMetaBlock = LinkMetaBlock.builder()
                .signature(Signature.builder()
                    .sig(signature)
                    .keyAlgorithm(KeyAlgorithm.EC)
                    .hashAlgorithm(HashAlgorithm.SHA384)
                    .build())
                .link(link).build();
        Link link = linkMetaBlock.getLink();
        Signature sig = linkMetaBlock.getSignature();

        ArgosError argosError = assertThrows(ArgosError.class, () -> SignatureValidator.isValid(link, sig, ecPair));
        assertThat(argosError.getMessage(), is("Odd number of characters."));

    }
    
    @Test
    void isValidStatement() {

        Envelope e = DATA_MAP.get("at2").getEnvelope();
    	//Signature signature = CryptoHelper.sign(ecPair, "test".toCharArray(), serializer.serialize(st));
    	
        assertThat(SignatureValidator.isValid(e.getPayload(), e.getSignatures().get(0), AttestationData.ecPair), is(true));
    }

    @Test
    void inValidSignatureStatement() throws NoSuchAlgorithmException, OperatorCreationException, PemGenerationException {

        Envelope e = DATA_MAP.get("at2").getEnvelope();
        
        KeyPair other = CryptoHelper.createKeyPair("test".toCharArray());
    	
        assertThat(SignatureValidator.isValid(e.getPayload(), e.getSignatures().get(0), other), is(false));

    }

    @Test
    void inValidSignatureStatementTrhows() {
        Signature sig = Signature.builder()
                .sig("foo")
                .keyAlgorithm(KeyAlgorithm.EC)
                .hashAlgorithm(HashAlgorithm.SHA384)
                .build();
        Statement st = DATA_MAP.get("at1").getEnvelope().getPayload();

        ArgosError argosError = assertThrows(ArgosError.class, () -> SignatureValidator.isValid(st, sig, ecPair));
        assertThat(argosError.getMessage(), is("Odd number of characters."));

    }
    
    @Test
    void testConstructorIsPrivate() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
      Constructor<SignatureValidator> constructor = SignatureValidator.class.getDeclaredConstructor();
      assertThat(Modifier.isPrivate(constructor.getModifiers()), is(true));
      constructor.setAccessible(true);
      constructor.newInstance();
    }
}
