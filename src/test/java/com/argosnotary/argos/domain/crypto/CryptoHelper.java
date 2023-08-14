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
package com.argosnotary.argos.domain.crypto;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.Security;

import org.apache.commons.codec.binary.Hex;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PKCS8Generator;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcaPKCS8Generator;
import org.bouncycastle.openssl.jcajce.JceOpenSSLPKCS8DecryptorProviderBuilder;
import org.bouncycastle.openssl.jcajce.JceOpenSSLPKCS8EncryptorBuilder;
import org.bouncycastle.operator.InputDecryptorProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.OutputEncryptor;
import org.bouncycastle.pkcs.PKCS8EncryptedPrivateKeyInfo;
import org.bouncycastle.pkcs.PKCSException;
import org.bouncycastle.util.io.pem.PemGenerationException;
import org.bouncycastle.util.io.pem.PemObject;

import com.argosnotary.argos.domain.ArgosError;
import com.argosnotary.argos.domain.crypto.signing.SignatureAlgorithm;

public class CryptoHelper {
	
	static {
		Security.addProvider(new BouncyCastleProvider());
	}

    public static Signature sign(KeyPair keyPair, char[] keyPassphrase, String jsonRepresentation) {
    	Signature sig = Signature.builder().keyId(keyPair.getKeyId()).build();
    	try {
			sig.setSig(createSignature(decryptPrivateKey(keyPair, keyPassphrase), jsonRepresentation, sig.getAlgorithm()));
		} catch (GeneralSecurityException e) {
            throw new ArgosError(e.getMessage(), e);
		}
        return sig;
    }

    private static String createSignature(PrivateKey privateKey, String jsonRepr, SignatureAlgorithm algorithm) throws GeneralSecurityException {
        java.security.Signature privateSignature = java.security.Signature.getInstance(algorithm.getStringValue());
        privateSignature.initSign(privateKey);
        privateSignature.update(jsonRepr.getBytes(StandardCharsets.UTF_8));
        return Hex.encodeHexString(privateSignature.sign());
    }


    public static PrivateKey decryptPrivateKey(KeyPair keyPair, char[] keyPassphrase) {
        try {
            PKCS8EncryptedPrivateKeyInfo encPKInfo = new PKCS8EncryptedPrivateKeyInfo(keyPair.getEncryptedPrivateKey());
            InputDecryptorProvider decProv = new JceOpenSSLPKCS8DecryptorProviderBuilder().setProvider("BC").build(keyPassphrase);
            PrivateKeyInfo pkInfo = encPKInfo.decryptPrivateKeyInfo(decProv);
            return new JcaPEMKeyConverter().setProvider("BC").getPrivateKey(pkInfo);
        } catch (IOException | PKCSException | OperatorCreationException e) {
            throw new ArgosError(e.getMessage(), e);
        }
    }
    
	public static KeyPair createKeyPair(char[] passphrase) throws NoSuchAlgorithmException, OperatorCreationException, PemGenerationException {
		KeyPairGenerator generator = KeyPairGenerator.getInstance(KeyAlgorithm.EC.name());
		java.security.KeyPair keyPair = generator.generateKeyPair();
        JceOpenSSLPKCS8EncryptorBuilder encryptorBuilder = new JceOpenSSLPKCS8EncryptorBuilder(PKCS8Generator.AES_256_CBC).setProvider("BC");  
        OutputEncryptor encryptor = encryptorBuilder
        		.setRandom(new SecureRandom())
        		.setPassword(passphrase).build();
      
        JcaPKCS8Generator gen2 = new JcaPKCS8Generator(keyPair.getPrivate(), encryptor);  
        PemObject obj2 = gen2.generate();
        return new KeyPair(KeyIdProvider.computeKeyId(keyPair.getPublic()), 
        		keyPair.getPublic().getEncoded(), obj2.getContent());
	}

}
