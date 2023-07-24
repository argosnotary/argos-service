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
package com.argosnotary.argos.service.rest;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.argosnotary.argos.domain.crypto.KeyPair;
import com.argosnotary.argos.domain.crypto.PublicKey;
import com.argosnotary.argos.domain.crypto.Signature;
import com.argosnotary.argos.domain.crypto.signing.SignatureValidator;
import com.argosnotary.argos.domain.layout.Layout;
import com.argosnotary.argos.domain.link.Link;
import com.argosnotary.argos.service.account.AccountService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class SignatureValidatorService {

    private final AccountService accountService;

    public boolean validateSignature(Layout layout, Signature signature) {
    	try {
    		Optional<java.security.PublicKey> key = getPublicKey(signature);
    		if (key.isEmpty() || !SignatureValidator.isValid(layout, signature, key.get())) {
			    return false;
			}
		} catch (GeneralSecurityException | IOException e) {
			log.error(e.getMessage());
			return false;
		}
    	return true;
    }

    public boolean validateSignature(Link link, Signature signature) {
        try {
        	Optional<java.security.PublicKey> key = getPublicKey(signature);
			if (key.isEmpty() || !SignatureValidator.isValid(link, signature, key.get())) {
			    return false;
			}
		} catch (GeneralSecurityException | IOException e) {
			log.error(e.getMessage());
			return false;
		}
        return true;
    }

    private Optional<java.security.PublicKey> getPublicKey(Signature signature) throws GeneralSecurityException, IOException {
    	Optional<KeyPair> keyPair = accountService.findKeyPairByKeyId(signature.getKeyId());
    	if (keyPair.isEmpty()) {
    		return Optional.empty();
    	}
    	return Optional.of(PublicKey.instance(keyPair.get().getPublicKey()));
    }

}
