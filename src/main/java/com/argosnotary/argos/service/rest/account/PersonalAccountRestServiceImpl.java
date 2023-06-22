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
				.map(account -> (PersonalAccount) account)
				.orElseThrow(this::accountNotFound);
		personalAccountService.activateNewKey(personalAccount, keyPair);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

    private void validateKeyId(KeyPair keyPair) {
        if (!keyPair.getKeyId().equals(KeyIdProvider.computeKeyId(keyPair.getPublicKey()))) {
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
		ResponseEntity<RestPersonalAccount> acc = accountSecurityContext.getAuthenticatedAccount()
				.map(account -> (PersonalAccount) account)
				.map(personalAccountMapper::convertToRestPersonalAccount)
				.map(ResponseEntity::ok)
				.orElseThrow(this::accountNotFound);
		return accountSecurityContext.getAuthenticatedAccount()
				.map(account -> (PersonalAccount) account)
				.map(personalAccountMapper::convertToRestPersonalAccount)
				.map(ResponseEntity::ok)
				.orElseThrow(this::accountNotFound);
	}

	private ResponseStatusException accountNotFound() {
		return new ResponseStatusException(HttpStatus.NOT_FOUND, "personal account not found");
	}

}
