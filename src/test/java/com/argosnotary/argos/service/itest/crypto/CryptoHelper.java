/*
 * Argos Notary - A new way to secure the Software Supply Chain
 *
 * Copyright (C) 2019 - 2020 Rabobank Nederland
 * Copyright (C) 2019 - 2024 Gerard Borst <gerard.borst@argosnotary.com>
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
package com.argosnotary.argos.service.itest.crypto;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PKCS8Generator;
import org.bouncycastle.openssl.jcajce.JcaPKCS8Generator;
import org.bouncycastle.openssl.jcajce.JceOpenSSLPKCS8EncryptorBuilder;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.OutputEncryptor;
import org.bouncycastle.pkcs.PKCSException;
import org.bouncycastle.util.io.pem.PemGenerationException;
import org.bouncycastle.util.io.pem.PemObject;

import com.argosnotary.argos.service.itest.rest.api.model.RestAttestation;
import com.argosnotary.argos.service.itest.rest.api.model.RestHashAlgorithm;
import com.argosnotary.argos.service.itest.rest.api.model.RestInTotoStatement;
import com.argosnotary.argos.service.itest.rest.api.model.RestKeyAlgorithm;
import com.argosnotary.argos.service.itest.rest.api.model.RestKeyPair;
import com.argosnotary.argos.service.itest.rest.api.model.RestLayoutMetaBlock;
import com.argosnotary.argos.service.itest.rest.api.model.RestLinkMetaBlock;
import com.argosnotary.argos.service.itest.rest.api.model.RestSignature;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CryptoHelper {
	
	static {
	    Security.addProvider(new BouncyCastleProvider());
	}
	
	public CryptoHelper() {
	}
    
	public String signLayout(String password, HashMap keyPairJson, HashMap restLayoutMetaBlockJson) throws JsonProcessingException, OperatorCreationException, GeneralSecurityException, IOException, PKCSException {

        ObjectMapper objectMapper = new ObjectMapper();
		RestKeyPair keyPair = objectMapper.convertValue(keyPairJson, RestKeyPair.class);
		RestLayoutMetaBlock restLayoutMetaBlock = objectMapper.convertValue(restLayoutMetaBlockJson, RestLayoutMetaBlock.class);
		
		String ser = new JsonSigningSerializer().serialize(restLayoutMetaBlock.getLayout());
		RestSignature signature = sign(keyPair, password.toCharArray(), new JsonSigningSerializer().serialize(restLayoutMetaBlock.getLayout()));
        List<RestSignature> signatures = new ArrayList<>(restLayoutMetaBlock.getSignatures());
        signatures.add(signature);
        restLayoutMetaBlock.setSignatures(signatures);
        return objectMapper.writeValueAsString(restLayoutMetaBlock);
    }
	
	public String signLink(String password, HashMap keyPairJson, HashMap restLinkMetaBlockJson) throws JsonProcessingException, OperatorCreationException, GeneralSecurityException, IOException, PKCSException {
		ObjectMapper objectMapper = new ObjectMapper();
		RestKeyPair keyPair = objectMapper.convertValue(keyPairJson, RestKeyPair.class);
		RestLinkMetaBlock restLinkMetaBlock = objectMapper.convertValue(restLinkMetaBlockJson, RestLinkMetaBlock.class);
		
		RestSignature signature = sign(keyPair, password.toCharArray(), new JsonSigningSerializer().serialize(restLinkMetaBlock.getLink()));
        restLinkMetaBlock.setSignature(signature);
        
        return objectMapper.writeValueAsString(restLinkMetaBlock);

    }
	
	public String signAttestation(String password, HashMap keyPairJson, HashMap restAttestationJson) throws JsonProcessingException, OperatorCreationException, GeneralSecurityException, IOException, PKCSException {
		ObjectMapper objectMapper = new ObjectMapper();
		RestKeyPair keyPair = objectMapper.convertValue(keyPairJson, RestKeyPair.class);
		RestAttestation restAttestation = objectMapper.convertValue(restAttestationJson, RestAttestation.class);
		
		RestSignature signature = sign(keyPair, password.toCharArray(), new JsonSigningSerializer().serialize(restAttestation.getEnvelope().getPayload()));
		restAttestation.getEnvelope().getSignatures().add(signature);
        
        String ff = objectMapper.writeValueAsString(restAttestation);
        
        return objectMapper.writeValueAsString(restAttestation);

    }
	
	public static RestKeyPair createKeyPair(char[] passphrase) throws NoSuchAlgorithmException, OperatorCreationException, PemGenerationException {
		KeyPairGenerator generator = KeyPairGenerator.getInstance(RestKeyAlgorithm.EC.name());
		java.security.KeyPair keyPair = generator.generateKeyPair();
        JceOpenSSLPKCS8EncryptorBuilder encryptorBuilder = new JceOpenSSLPKCS8EncryptorBuilder(PKCS8Generator.AES_256_CBC).setProvider("BC");  
        OutputEncryptor encryptor = encryptorBuilder
        		.setRandom(new SecureRandom())
        		.setPassword(passphrase).build();
      
        JcaPKCS8Generator gen2 = new JcaPKCS8Generator(keyPair.getPrivate(), encryptor);  
        PemObject obj2 = gen2.generate();
        RestKeyPair restKeyPair = new RestKeyPair();
        restKeyPair.keyId(computeKeyId(keyPair.getPublic()));
        restKeyPair.pub(keyPair.getPublic().getEncoded());
        restKeyPair.encryptedPrivateKey(obj2.getContent());
        return restKeyPair;
	}
	
	private static String computeKeyId(PublicKey publicKey) {
        return DigestUtils.sha256Hex(publicKey.getEncoded());
    }
	
	public static final RestKeyAlgorithm DEFAULT_KEY_ALGORITHM = RestKeyAlgorithm.EC;
	public static final RestHashAlgorithm DEFAULT_HASH_ALGORITHM = RestHashAlgorithm.SHA384;

    public static RestSignature sign(RestKeyPair keyPair, char[] keyPassphrase, String jsonRepresentation) throws OperatorCreationException, GeneralSecurityException, IOException, PKCSException {
    	RestSignature sig = new RestSignature();
    	sig.setKeyId(keyPair.getKeyId());
    	sig.setHashAlgorithm(DEFAULT_HASH_ALGORITHM);
    	sig.setKeyAlgorithm(DEFAULT_KEY_ALGORITHM);
    	sig.setSig(createSignature(CryptUtil.decryptPrivateKey(keyPassphrase, keyPair.getEncryptedPrivateKey()), 
					jsonRepresentation, SignatureAlgorithm.getSignatureAlgorithm(sig.getKeyAlgorithm(), sig.getHashAlgorithm())));
		return sig;
    }

    private static String createSignature(PrivateKey privateKey, String jsonRepr, SignatureAlgorithm algorithm) throws GeneralSecurityException {
        java.security.Signature privateSignature = java.security.Signature.getInstance(algorithm.getStringValue());
        privateSignature.initSign(privateKey);
        privateSignature.update(jsonRepr.getBytes(StandardCharsets.UTF_8));
        return Hex.encodeHexString(privateSignature.sign());
    }

}
