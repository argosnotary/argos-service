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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.util.io.pem.PemGenerationException;
import org.junit.jupiter.api.Test;

import com.argosnotary.argos.domain.ArgosError;

class KeyPairTest {
	@Test
	void createKeyPairAndSignature() throws OperatorCreationException, GeneralSecurityException, IOException {
		String passphrase = "test";
		KeyPair keyPair = CryptoHelper.createKeyPair("test".toCharArray());
		byte[] encodedKey = Base64.getEncoder().encode(keyPair.getEncryptedPrivateKey());
		String jsonTempl = "{\n" + 
				"  \"keyId\": \"%s\",\n" + 
				"  \"pub\": \"%s\",\n" + 
				"  \"encryptedPrivateKey\": \"%s\",\n" +
				"  \"passphrase\": \"%s\"\n" +
				"}";
		System.out.println("id:           "+keyPair.getKeyId());
		System.out.println("encryptedKey: "+new String(encodedKey));
		System.out.println("publicKey:    "+new String(Base64.getEncoder().encode(keyPair.getPub())));
		System.out.println(String.format(jsonTempl, 
				keyPair.getKeyId(), 
				new String(Base64.getEncoder().encode(keyPair.getPub())),
				new String(Base64.getEncoder().encode(keyPair.getEncryptedPrivateKey())), 
				passphrase));
		Signature signature = CryptoHelper.sign(keyPair, passphrase.toCharArray(), "zomaar");
		System.out.println("signature: "+new String(Base64.getEncoder().encode(signature.getSig().getBytes())));
		assertThat(signature.getKeyId(), is(keyPair.getKeyId()));
		assertThat(signature.getKeyAlgorithm(), is(KeyAlgorithm.valueOf(PublicKey.getJavaPublicKey(keyPair.getPub()).getAlgorithm())));
		
	}
	
	@Test
    void toStringTest() throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, OperatorCreationException, PemGenerationException {
        KeyPair keyPair = new KeyPair("keyId", "publicKey".getBytes(), "encryptedPrivateKey".getBytes());
        assertThat(keyPair.toString(), is("KeyPair(super=PublicKey(keyId=keyId, pub=[112, 117, 98, 108, 105, 99, 75, 101, 121]), encryptedPrivateKey=[101, 110, 99, 114, 121, 112, 116, 101, 100, 80, 114, 105, 118, 97, 116, 101, 75, 101, 121])"));
        
    }
	
	@Test
    void setterAndConstructorTest() throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, OperatorCreationException, PemGenerationException {
        KeyPair keyPair = new KeyPair("keyId", "publicKey".getBytes(), "encryptedPrivateKey".getBytes());
        assertThat(keyPair.getKeyId(), is("keyId"));
        assertThat(new String(keyPair.getEncryptedPrivateKey()), is("encryptedPrivateKey"));
        assertThat(new String(keyPair.getPub()), is("publicKey"));
        
        
    }
	
	@Test
    void throwInDecryptTest() {
        KeyPair keyPair = new KeyPair("keyId", "publicKey".getBytes(), "encryptedPrivateKey".getBytes());
        char[] a = "bla".toCharArray();
        Throwable exception = assertThrows(ArgosError.class, () -> {
        	CryptoHelper.decryptPrivateKey(keyPair, a);
          });
        
        assertEquals("corrupted stream - out of bounds length found: 101 >= 101", exception.getMessage());
        
        
    }

}
