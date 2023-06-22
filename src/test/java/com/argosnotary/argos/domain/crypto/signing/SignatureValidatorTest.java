/*
 * Argos Notary - A new way to secure the Software Supply Chain
 *
 * Copyright (C) 2019 - 2020 Rabobank Nederland
 * Copyright (C) 2019 - 2022 Gerard Borst <gerard.borst@argosnotary.com>
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

import com.argosnotary.argos.domain.ArgosError;
import com.argosnotary.argos.domain.crypto.HashAlgorithm;
import com.argosnotary.argos.domain.crypto.KeyAlgorithm;
import com.argosnotary.argos.domain.layout.Layout;
import com.argosnotary.argos.domain.layout.LayoutMetaBlock;
import com.argosnotary.argos.domain.link.Artifact;
import com.argosnotary.argos.domain.link.Link;
import com.argosnotary.argos.domain.link.LinkMetaBlock;
import org.apache.commons.codec.binary.Hex;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;

import java.util.List;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.ECGenParameterSpec;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SignatureValidatorTest {

    private Link link;
    private Layout layout;
    //private KeyPair pair;
    private KeyPair ecPair;

    @BeforeEach
    void setUp() throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException {
        link = Link.builder()
                .products(singletonList(Artifact.builder().hash("hash2").uri("/path/tofile2").build()))
                .materials(singletonList(Artifact.builder().hash("hash").uri("/path/tofile").build())).build();

        KeyPairGenerator generator = KeyPairGenerator.getInstance("EC");
        generator.initialize(new ECGenParameterSpec("secp256r1"));

        ecPair = generator.generateKeyPair();
        
        layout = Layout.builder().steps(new ArrayList<>()).build();
        
    }

    @Test
    void isValid() throws InvalidKeyException, SignatureException, NoSuchAlgorithmException {
    	Signature privateSignature = Signature.getInstance("SHA384withECDSA");
        privateSignature.initSign(ecPair.getPrivate());

        privateSignature.update(new JsonSigningSerializer().serialize(link).getBytes(UTF_8));

        String signature = Hex.encodeHexString(privateSignature.sign());

        LinkMetaBlock linkMetaBlock = LinkMetaBlock.builder()
                .signature(com.argosnotary.argos.domain.crypto.Signature.builder()
                    .signature(signature)
                    .keyAlgorithm(KeyAlgorithm.EC)
                    .hashAlgorithm(HashAlgorithm.SHA384)
                    .build())
                .link(link).build();
        assertThat(SignatureValidator.isValid(linkMetaBlock.getLink(), linkMetaBlock.getSignature(), ecPair.getPublic()), is(true));

        privateSignature.initSign(ecPair.getPrivate());

        privateSignature.update(new JsonSigningSerializer().serialize(layout).getBytes(UTF_8));

        signature = Hex.encodeHexString(privateSignature.sign());
        
        List<com.argosnotary.argos.domain.crypto.Signature> signatures = new ArrayList<>();
        signatures.add(com.argosnotary.argos.domain.crypto.Signature.builder()
                .signature(signature)
                .keyAlgorithm(KeyAlgorithm.EC)
                .hashAlgorithm(HashAlgorithm.SHA384)
                .build());
        LayoutMetaBlock layoutMetaBlock = LayoutMetaBlock.builder()
                .signatures(signatures)
                .layout(layout)
                .build();
        assertThat(SignatureValidator.isValid(layoutMetaBlock.getLayout(), layoutMetaBlock.getSignatures().get(0), ecPair.getPublic()), is(true));
    }
    
    @Test
    void isValidEC() throws InvalidKeyException, SignatureException, NoSuchAlgorithmException {

        Signature privateSignature = Signature.getInstance("SHA384withECDSA");
        privateSignature.initSign(ecPair.getPrivate());

        privateSignature.update(new JsonSigningSerializer().serialize(link).getBytes(UTF_8));

        String signature = Hex.encodeHexString(privateSignature.sign());

        LinkMetaBlock linkMetaBlock = LinkMetaBlock.builder()
                .signature(com.argosnotary.argos.domain.crypto.Signature.builder()
                    .signature(signature)
                    .keyAlgorithm(KeyAlgorithm.EC)
                    .hashAlgorithm(HashAlgorithm.SHA384)
                    .build())
                .link(link).build();
        assertThat(SignatureValidator.isValid(linkMetaBlock.getLink(), linkMetaBlock.getSignature(), ecPair.getPublic()), is(true));
    }

    @Test
    void isNotValid() throws InvalidKeyException, SignatureException, NoSuchAlgorithmException {

        Signature privateSignature = Signature.getInstance("SHA384withECDSA");
        privateSignature.initSign(ecPair.getPrivate());

        privateSignature.update(new JsonSigningSerializer().serialize(link).getBytes(UTF_8));
        String signature = Hex.encodeHexString(privateSignature.sign());

        link.setStepName("extra");

        LinkMetaBlock linkMetaBlock = LinkMetaBlock.builder()
                .signature(com.argosnotary.argos.domain.crypto.Signature.builder()
                    .signature(signature)
                    .keyAlgorithm(KeyAlgorithm.EC)
                    .hashAlgorithm(HashAlgorithm.SHA384)
                    .build())
                .link(link).build();
        assertThat(SignatureValidator.isValid(linkMetaBlock.getLink(), linkMetaBlock.getSignature(), ecPair.getPublic()), is(false));
        
        privateSignature.initSign(ecPair.getPrivate());

        privateSignature.update(new JsonSigningSerializer().serialize(layout).getBytes(UTF_8));

        signature = Hex.encodeHexString(privateSignature.sign());
        
        layout.setKeys(new ArrayList<>());
        
        List<com.argosnotary.argos.domain.crypto.Signature> signatures = new ArrayList<>();
        signatures.add(com.argosnotary.argos.domain.crypto.Signature.builder()
                .signature(signature)
                .keyAlgorithm(KeyAlgorithm.EC)
                .hashAlgorithm(HashAlgorithm.SHA384)
                .build());
        LayoutMetaBlock layoutMetaBlock = LayoutMetaBlock.builder()
                .signatures(signatures)
                .layout(layout)
                .build();
        assertThat(SignatureValidator.isValid(layoutMetaBlock.getLayout(), layoutMetaBlock.getSignatures().get(0), ecPair.getPublic()), is(false));
    }

    @Test
    void inValidSignature() {
        String signature = "bla";
        LinkMetaBlock linkMetaBlock = LinkMetaBlock.builder()
                .signature(com.argosnotary.argos.domain.crypto.Signature.builder()
                    .signature(signature)
                    .keyAlgorithm(KeyAlgorithm.EC)
                    .hashAlgorithm(HashAlgorithm.SHA384)
                    .build())
                .link(link).build();
        Link link = linkMetaBlock.getLink();
        com.argosnotary.argos.domain.crypto.Signature sig = linkMetaBlock.getSignature();
        PublicKey pub = ecPair.getPublic();

        ArgosError argosError = assertThrows(ArgosError.class, () -> SignatureValidator.isValid(link, sig, pub));
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
