/*
 * Argos Notary - A new way to secure the Software Supply Chain
 *
 * Copyright (C) 2019 - 2020 Rabobank Nederland
 * Copyright (C) 2019 - 2024 Gerard Borst <gerard.borst@argosnotary.com>
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
package com.argosnotary.argos.service.rest.account;

import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.argosnotary.argos.domain.account.Account;
import com.argosnotary.argos.domain.account.PersonalAccount;
import com.argosnotary.argos.domain.crypto.KeyIdProvider;
import com.argosnotary.argos.domain.crypto.KeyPair;
import com.argosnotary.argos.service.account.AccountSecurityContext;
import com.argosnotary.argos.service.account.PersonalAccountService;
import com.argosnotary.argos.service.auditlog.AuditLog;
import com.argosnotary.argos.service.openapi.rest.model.RestKeyPair;
import com.argosnotary.argos.service.openapi.rest.model.RestPersonalAccount;
import com.argosnotary.argos.service.openapi.rest.model.RestPublicKey;
import com.argosnotary.argos.service.rest.KeyPairMapper;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class PersonalAccountRestServiceImpl implements PersonalAccountRestService {

    private final AccountSecurityContext accountSecurityContext;

    private final KeyPairMapper keyPairMapper;
    private final PersonalAccountService personalAccountService;
    private final PersonalAccountMapper personalAccountMapper;

	@PreAuthorize("isAuthenticated()")
    @AuditLog
	@Override
	public ResponseEntity<Void> createKey(@Valid RestKeyPair restKeyPair) {
		KeyPair keyPair = keyPairMapper.convertFromRestKeyPair(restKeyPair);
		validateKeyId(keyPair);
		PersonalAccount personalAccount = accountSecurityContext.getAuthenticatedAccount()
				.map(PersonalAccount.class::cast)
				.orElseThrow(this::accountNotFound);
		personalAccountService.activateNewKey(personalAccount, keyPair);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

    private void validateKeyId(KeyPair keyPair) {
        if (!keyPair.getKeyId().equals(KeyIdProvider.computeKeyId(keyPair.getPub()))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid key id : " + keyPair.getKeyId());
        }
    }

	@Override
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<RestKeyPair> getKeyPair() {
        Account account = accountSecurityContext
                .getAuthenticatedAccount().orElseThrow(this::accountNotFound);
        KeyPair keyPair = Optional.ofNullable(account.getActiveKeyPair()).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "no active keypair found for account: " + account.getName()));
        return new ResponseEntity<>(keyPairMapper.convertToRestKeyPair(keyPair), HttpStatus.OK);
	}

	@Override
    @PreAuthorize("isAuthenticated()")
	public ResponseEntity<RestPersonalAccount> getPersonalAccountById(UUID accountId) {
        return personalAccountService.getPersonalAccountById(accountId)
                .map(personalAccountMapper::convertToRestPersonalAccountIdentity)
                .map(ResponseEntity::ok).orElseThrow(this::accountNotFound);
	}

	@Override
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<RestPublicKey> getPersonalAccountKeyById(UUID accountId) {
        PersonalAccount account = personalAccountService.getPersonalAccountById(accountId).orElseThrow(this::accountNotFound);
        return ResponseEntity.ok(Optional.ofNullable(account.getActiveKeyPair()).map(keyPairMapper::convertToRestPublicKey)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "no active keypair found for account: " + account.getName())));
	}

	@Override
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<RestPersonalAccount> whoAmI() {
		return accountSecurityContext.getAuthenticatedAccount()
				.map(PersonalAccount.class::cast)
				.map(personalAccountMapper::convertToRestPersonalAccount)
				.map(ResponseEntity::ok)
				.orElseThrow(this::accountNotFound);
	}

	private ResponseStatusException accountNotFound() {
		return new ResponseStatusException(HttpStatus.NOT_FOUND, "personal account not found");
	}

}
