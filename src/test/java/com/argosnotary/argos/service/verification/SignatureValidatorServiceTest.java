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
package com.argosnotary.argos.service.verification;

import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;

import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.util.io.pem.PemGenerationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.argosnotary.argos.domain.attest.Attestation;
import com.argosnotary.argos.domain.attest.AttestationData;
import com.argosnotary.argos.domain.attest.Envelope;
import com.argosnotary.argos.domain.crypto.CryptoHelper;
import com.argosnotary.argos.domain.crypto.KeyIdProvider;
import com.argosnotary.argos.domain.crypto.KeyPair;
import com.argosnotary.argos.domain.crypto.PublicKey;
import com.argosnotary.argos.domain.crypto.Signature;
import com.argosnotary.argos.domain.crypto.signing.JsonSigningSerializer;
import com.argosnotary.argos.domain.layout.Layout;
import com.argosnotary.argos.domain.layout.Step;
import com.argosnotary.argos.domain.link.Artifact;
import com.argosnotary.argos.domain.link.Link;
import com.argosnotary.argos.service.account.AccountService;


@ExtendWith(MockitoExtension.class)
class SignatureValidatorServiceTest {
	
	private static final Map<String, Attestation> DATA_MAP = AttestationData.createTestData();
    private char[] PASSPHRASE = "test".toCharArray();

    private static final String KEY_ID = "keyId";
    
    private static final byte[] key = Base64.getDecoder().decode("MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE6UI21H3Ti3fWK98DJPiLxaxHuQBB3P28DeskZWlHQSPi104E7xi49sVMJTDaOHNs9YJVqI2fnvCFtGPk3NTCgA==");
    
    @Mock
    private AccountService accountService;
    
    private SignatureValidatorService service;

	private String keyId;
    private String keyId2;

    private Layout layout;
    private Layout layout2;
    private Layout layout3;

    private Signature layoutSignature;
    private Signature layoutSignature2;
    private Signature linkSignature;
    private Signature linkSignature2;
    
    private KeyPair pair;
    private KeyPair pair2;

    private com.argosnotary.argos.domain.crypto.PublicKey domainPublicKey;
    private com.argosnotary.argos.domain.crypto.PublicKey domainPublicKey2;

    private Link link;
    private Link link2;

    @BeforeEach
    void setUp() throws GeneralSecurityException, OperatorCreationException, PemGenerationException {
    	service = new SignatureValidatorService(accountService);
    	
    	Step step = Step.builder().build();

        // valid
        pair = CryptoHelper.createKeyPair(PASSPHRASE);
        keyId = KeyIdProvider.computeKeyId(pair.getPub());
        domainPublicKey = new PublicKey(keyId, pair.getPub());
        layout = Layout.builder()
                .steps(List.of(step))
        		.keys(List.of(domainPublicKey)).build();
        link = Link.builder()
                .products(singletonList(Artifact.builder().hash("hash2").uri("/path/tofile2").build()))
                .materials(singletonList(Artifact.builder().hash("hash").uri("/path/tofile").build())).build();
        layoutSignature = CryptoHelper.sign(pair, PASSPHRASE, new JsonSigningSerializer().serialize(layout));
        linkSignature = CryptoHelper.sign(pair, PASSPHRASE, new JsonSigningSerializer().serialize(link));
        
        // not valid
        pair2 = CryptoHelper.createKeyPair(PASSPHRASE);
        keyId2 = KeyIdProvider.computeKeyId(pair2.getPub());
        domainPublicKey2 = new PublicKey(keyId2, pair2.getPub());
        layout2 = Layout.builder()
                .steps(List.of(step))
        		.keys(List.of(domainPublicKey, domainPublicKey2)).build();
        layoutSignature2 = CryptoHelper.sign(pair2, PASSPHRASE, new JsonSigningSerializer().serialize(layout2));
        linkSignature2 = CryptoHelper.sign(pair2, PASSPHRASE, new JsonSigningSerializer().serialize(link));
        // make invalid
        layoutSignature2.setKeyId(keyId);
        linkSignature2.setKeyId(keyId);
    }

    @Test
    void validLayoutSignature() {
        when(accountService.findPublicKeyByKeyId(keyId)).thenReturn(Optional.of(pair));
        assertThat(service.validateSignature(layout, layoutSignature), is(true));
    }

    @Test
    void inValidLayoutSignature() throws GeneralSecurityException {
        when(accountService.findPublicKeyByKeyId(keyId)).thenReturn(Optional.of(pair));

        assertThat(service.validateSignature(layout2, layoutSignature2), is(false));
    }

    @Test
    void validLayoutSignatureExc() {
        when(accountService.findPublicKeyByKeyId(keyId)).thenReturn(Optional.of(pair));
        assertThat(service.validateSignature(layout, layoutSignature), is(true));
    }

    @Test
    void keyNotFoundLayoutSignature() {
        when(accountService.findPublicKeyByKeyId(keyId)).thenReturn(Optional.empty());

        assertThat(service.validateSignature(layout, layoutSignature), is(false));
    }
    
    @Test
    void validLinkSignature() {
        when(accountService.findPublicKeyByKeyId(keyId)).thenReturn(Optional.of(pair));
        assertThat(service.validateSignature(link, linkSignature), is(true));
    	
    }
    
    @Test
    void inValidLinkSignature() {
        when(accountService.findPublicKeyByKeyId(keyId)).thenReturn(Optional.of(pair));

        assertThat(service.validateSignature(link, linkSignature2), is(false));
    }
    
    @Test
    void keyNotFoundLinkSignature() {
        when(accountService.findPublicKeyByKeyId(keyId)).thenReturn(Optional.empty());
        assertThat(service.validateSignature(link, linkSignature), is(false));
    }
    
    @Test
    void validStatementSignatureKeyFound() {
    	Envelope e = DATA_MAP.get("at1").getEnvelope();
    	when(accountService.findPublicKeyByKeyId(e.getSignatures().get(0).getKeyId())).thenReturn(Optional.of(AttestationData.ecPair));
        assertThat(service.validateSignature(e.getPayload(), e.getSignatures().get(0)), is(true));
    	
    }
    
    @Test
    void invalidStatementSignatureKeyNotFound() {
    	Envelope e = DATA_MAP.get("at1").getEnvelope();
    	when(accountService.findPublicKeyByKeyId(e.getSignatures().get(0).getKeyId())).thenReturn(Optional.empty());
        assertThat(service.validateSignature(e.getPayload(), e.getSignatures().get(0)), is(false));
    	
    }
    
    @Test
    void invalidStatementSignatureKeyFound() throws NoSuchAlgorithmException, OperatorCreationException, PemGenerationException {
        
        KeyPair other = CryptoHelper.createKeyPair("test".toCharArray());
    	Envelope e = DATA_MAP.get("at1").getEnvelope();
    	when(accountService.findPublicKeyByKeyId(e.getSignatures().get(0).getKeyId())).thenReturn(Optional.of(other));
        assertThat(service.validateSignature(e.getPayload(), e.getSignatures().get(0)), is(false));
    	
    }
    
    @Test
    void validateStatementSignatureWithKey() throws NoSuchAlgorithmException, OperatorCreationException, PemGenerationException {
    	KeyPair other = CryptoHelper.createKeyPair("test".toCharArray());
	
    	Envelope e = DATA_MAP.get("at1").getEnvelope();
        assertThat(service.validateSignature(e.getPayload(), e.getSignatures().get(0), Optional.of(AttestationData.ecPair)), is(true));

        assertThat(service.validateSignature(e.getPayload(), e.getSignatures().get(0), Optional.empty()), is(false));

        assertThat(service.validateSignature(e.getPayload(), e.getSignatures().get(0), Optional.of(other)), is(false));
    	
    }
    

}
