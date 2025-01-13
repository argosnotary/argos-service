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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.PublicKey;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.bouncycastle.operator.OperatorCreationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.argosnotary.argos.domain.ArgosError;
import com.argosnotary.argos.domain.crypto.CryptoHelper;
import com.argosnotary.argos.domain.crypto.KeyPair;
import com.argosnotary.argos.domain.crypto.Signature;

class SignerTest {

    private static final char[] PASSWORD = "password".toCharArray();
    private static final char[] OTHER_PASSWORD = "other password".toCharArray();

    private KeyPair keyPair;
    private PublicKey publicKey;

    @BeforeEach
    void setUp() throws GeneralSecurityException, IOException, OperatorCreationException {
    	keyPair = CryptoHelper.createKeyPair(PASSWORD);
    	publicKey = com.argosnotary.argos.domain.crypto.PublicKey.instance(keyPair.getPub());
    }

    @Test
    void sign() throws DecoderException, GeneralSecurityException {
        Signature signature = CryptoHelper.sign(keyPair, PASSWORD, "string to sign");
        assertThat(signature.getKeyId(), is(keyPair.getKeyId()));

        java.security.Signature signatureValidator = java.security.Signature.getInstance("SHA384withECDSA");
        signatureValidator.initVerify(publicKey);
        signatureValidator.update("string to sign".getBytes(StandardCharsets.UTF_8));

        assertTrue(signatureValidator.verify(Hex.decodeHex(signature.getSig())));
    }
    
    @Test
    void signInvalidPassword() throws DecoderException, GeneralSecurityException {
    	Throwable exception = assertThrows(ArgosError.class, () -> {
    		CryptoHelper.sign(keyPair, OTHER_PASSWORD, "string to sign");
          });
    	assertEquals("unable to read encrypted data: Error finalising cipher", exception.getMessage());
    }
    
}
