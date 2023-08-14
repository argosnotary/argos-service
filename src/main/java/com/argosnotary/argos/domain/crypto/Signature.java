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

import java.security.GeneralSecurityException;

import com.argosnotary.argos.domain.crypto.signing.SignatureAlgorithm;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Builder
@Setter
@Getter
@EqualsAndHashCode(exclude={"sig"})
@ToString
public class Signature {
    private String keyId;
    private String sig;
    @Builder.Default
    private KeyAlgorithm keyAlgorithm = KeyAlgorithm.EC;
    @Builder.Default
    private HashAlgorithm hashAlgorithm = HashAlgorithm.SHA384;
    
    public SignatureAlgorithm getAlgorithm() throws GeneralSecurityException {
    	return SignatureAlgorithm.getAlgorithm(this.keyAlgorithm, this.hashAlgorithm);
    }
}
