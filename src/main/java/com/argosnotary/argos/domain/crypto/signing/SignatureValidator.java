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

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.security.GeneralSecurityException;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import com.argosnotary.argos.domain.ArgosError;
import com.argosnotary.argos.domain.attest.Statement;
import com.argosnotary.argos.domain.crypto.PublicKey;
import com.argosnotary.argos.domain.crypto.Signature;
import com.argosnotary.argos.domain.layout.Layout;
import com.argosnotary.argos.domain.link.Link;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SignatureValidator {

    public static boolean isValid(Link link, Signature signature, PublicKey key) {
        try {
        	return isValid(new JsonSigningSerializer().serialize(link), signature, getPublicKey(key));
		} catch (GeneralSecurityException | IOException e) {
            throw new ArgosError(e.getMessage(), e);
		}
    }

    public static boolean isValid(Layout layout, Signature signature, PublicKey key) {
        try {
        	return isValid(new JsonSigningSerializer().serialize(layout), signature, getPublicKey(key));
		} catch (GeneralSecurityException | IOException e) {
            throw new ArgosError(e.getMessage(), e);
		}
    }

    public static boolean isValid(Statement statement, Signature signature, PublicKey key) {
        try {
        	return isValid(new JsonSigningSerializer().serialize(statement), signature, getPublicKey(key));
		} catch (GeneralSecurityException | IOException e) {
            throw new ArgosError(e.getMessage(), e);
		}
    }

    private static boolean isValid(String signableJson, Signature signature, java.security.PublicKey publicKey) {
        try {
            java.security.Signature publicSignature = java.security.Signature.getInstance(signature.getAlgorithm().getStringValue());
            publicSignature.initVerify(publicKey);
            publicSignature.update(signableJson.getBytes(UTF_8));
            byte[] signatureBytes = Hex.decodeHex(signature.getSig());
            return publicSignature.verify(signatureBytes);
        } catch (GeneralSecurityException | DecoderException e) {
            throw new ArgosError(e.getMessage(), e);
        }
    }

    private static java.security.PublicKey getPublicKey(PublicKey key) throws GeneralSecurityException, IOException {
    	return PublicKey.instance(key.getPub());
    }
}
