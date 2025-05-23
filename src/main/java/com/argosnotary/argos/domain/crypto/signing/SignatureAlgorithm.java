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

import java.security.GeneralSecurityException;

import com.argosnotary.argos.domain.crypto.HashAlgorithm;
import com.argosnotary.argos.domain.crypto.KeyAlgorithm;

public enum SignatureAlgorithm {
	SHA_384_WITH_ECDSA("SHA384withECDSA"), SHA_256_WITH_RSA("SHA256withRSA");;
	
	String stringValue;
	
	SignatureAlgorithm(String stringValue) {
		this.stringValue = stringValue;
	}
	
	public String getStringValue() {
	    return stringValue;
	}
	
	public static SignatureAlgorithm getAlgorithm(KeyAlgorithm keyAlgorithm, HashAlgorithm hashAlgorithm) throws GeneralSecurityException {
		if (KeyAlgorithm.EC.equals(keyAlgorithm) && HashAlgorithm.SHA384.equals(hashAlgorithm)) {
    		return SHA_384_WITH_ECDSA;
    	}
		throw new GeneralSecurityException(String.format("Combination of algorithms [%s] and [%s] not supported", keyAlgorithm, hashAlgorithm));
	}

}
