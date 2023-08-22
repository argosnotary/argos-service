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
package com.argosnotary.argos.service.verification;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.argosnotary.argos.domain.attest.Statement;
import com.argosnotary.argos.domain.crypto.PublicKey;
import com.argosnotary.argos.domain.crypto.Signature;
import com.argosnotary.argos.domain.crypto.signing.SignatureValidator;
import com.argosnotary.argos.domain.layout.Layout;
import com.argosnotary.argos.domain.link.Link;
import com.argosnotary.argos.service.account.AccountService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SignatureValidatorService {

	private final AccountService accountService;

	public boolean validateSignature(Layout layout, Signature signature) {
		Optional<PublicKey> key = getPublicKey(signature);
		return validateSignature(layout, signature, key);
	}

	public boolean validateSignature(Link link, Signature signature) {
		Optional<PublicKey> key = getPublicKey(signature);
		return validateSignature(link, signature, key);
	}

	public boolean validateSignature(Layout layout, Signature signature, Optional<PublicKey> key) {
		return key.isPresent() && SignatureValidator.isValid(layout, signature, key.get());
	}

	public boolean validateSignature(Link link, Signature signature, Optional<PublicKey> key) {
		return key.isPresent() && SignatureValidator.isValid(link, signature, key.get());
	}

	public boolean validateSignature(Statement statement, Signature signature, Optional<PublicKey> key) {
		return key.isPresent() && SignatureValidator.isValid(statement, signature, key.get());
	}

	public boolean validateSignature(Statement statement, Signature signature) {
		Optional<PublicKey> key = getPublicKey(signature);
		return validateSignature(statement, signature, key);
	}


	private Optional<PublicKey> getPublicKey(Signature signature) {
		return accountService.findPublicKeyByKeyId(signature.getKeyId());
	}

}
