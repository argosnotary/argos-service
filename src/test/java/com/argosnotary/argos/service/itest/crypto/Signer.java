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
package com.argosnotary.argos.service.itest.crypto;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.Security;

import org.apache.commons.codec.binary.Hex;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.pkcs.PKCSException;

import com.argosnotary.argos.service.itest.rest.api.model.RestHashAlgorithm;
import com.argosnotary.argos.service.itest.rest.api.model.RestKeyAlgorithm;
import com.argosnotary.argos.service.itest.rest.api.model.RestKeyPair;
import com.argosnotary.argos.service.itest.rest.api.model.RestSignature;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Signer {
	
	static {
	    Security.addProvider(new BouncyCastleProvider());
	}

	
	public static final RestKeyAlgorithm DEFAULT_KEY_ALGORITHM = RestKeyAlgorithm.EC;
	public static final RestHashAlgorithm DEFAULT_HASH_ALGORITHM = RestHashAlgorithm.SHA384;

    public static RestSignature sign(RestKeyPair keyPair, char[] keyPassphrase, String jsonRepresentation) throws OperatorCreationException, GeneralSecurityException, IOException, PKCSException {
    	RestSignature sig = new RestSignature();
    	sig.setKeyId(keyPair.getKeyId());
    	sig.setHashAlgorithm(DEFAULT_HASH_ALGORITHM);
    	sig.setKeyAlgorithm(DEFAULT_KEY_ALGORITHM);
    	sig.setSignature(createSignature(CryptUtil.decryptPrivateKey(keyPassphrase, keyPair.getEncryptedPrivateKey()), 
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
