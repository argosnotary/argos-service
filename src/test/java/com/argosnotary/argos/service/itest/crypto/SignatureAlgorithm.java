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
package com.argosnotary.argos.service.itest.crypto;

import java.security.GeneralSecurityException;

import com.argosnotary.argos.service.itest.rest.api.model.RestHashAlgorithm;
import com.argosnotary.argos.service.itest.rest.api.model.RestKeyAlgorithm;

public enum SignatureAlgorithm {
	SHA_384_WITH_ECDSA("SHA384withECDSA"), SHA_256_WITH_RSA("SHA256withRSA");;
	
	String stringValue;
	
	SignatureAlgorithm(String stringValue) {
		this.stringValue = stringValue;
	}
	
	public String getStringValue() {
	    return stringValue;
	}
	
	public static SignatureAlgorithm getSignatureAlgorithm(RestKeyAlgorithm keyAlgorithm, RestHashAlgorithm hashAlgorithm) throws GeneralSecurityException {
    	if (RestKeyAlgorithm.EC == keyAlgorithm && RestHashAlgorithm.SHA384 == hashAlgorithm) {
    		return SHA_384_WITH_ECDSA;
    	} else {
    	    throw new GeneralSecurityException(String.format("Combination of algorithms [%s] and [%s] not supported", keyAlgorithm, hashAlgorithm));
    	}
	}

}
