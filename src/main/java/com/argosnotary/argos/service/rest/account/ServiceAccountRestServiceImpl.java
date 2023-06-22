package com.argosnotary.argos.service.rest.account;

import java.net.URI;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.argosnotary.argos.domain.account.Account;
import com.argosnotary.argos.domain.account.ServiceAccount;
import com.argosnotary.argos.domain.crypto.KeyPair;
import com.argosnotary.argos.domain.roles.Permission;
import com.argosnotary.argos.service.account.AccountSecurityContext;
import com.argosnotary.argos.service.account.ServiceAccountService;
import com.argosnotary.argos.service.openapi.rest.model.RestKeyPair;
import com.argosnotary.argos.service.openapi.rest.model.RestServiceAccount;
import com.argosnotary.argos.service.openapi.rest.model.RestServiceAccountKeyPair;
import com.argosnotary.argos.service.roles.PermissionCheck;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ServiceAccountRestServiceImpl implements ServiceAccountRestService {

    private final ServiceAccountMapper accountMapper;

    private final KeyPairMapper keyPairMapper;

    private final ServiceAccountService serviceAccountService;

    private final AccountSecurityContext accountSecurityContext;

	@Override
    @PermissionCheck(permissions = Permission.WRITE)
	public ResponseEntity<RestServiceAccount> createServiceAccount(UUID projectId,
			@Valid RestServiceAccount restServiceAccount) {
		verifyProjectId(projectId, restServiceAccount.getProjectId());
		ServiceAccount serviceAccount = accountMapper.convertFromRestServiceAccount(restServiceAccount);
        
		serviceAccount = serviceAccountService.createServiceAccount(serviceAccount);
        
		URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{serviceAccountId}")
                .buildAndExpand(serviceAccount.getId())
                .toUri();
        return ResponseEntity.created(location).body(accountMapper.convertToRestServiceAccount(serviceAccount));
	}

	@Override
    @PermissionCheck(permissions = Permission.WRITE)
	public ResponseEntity<RestKeyPair> createServiceAccountKeyById(UUID projectId, UUID serviceAccountId,
			RestServiceAccountKeyPair restServiceAccountKeyPair) {
		ServiceAccount serviceAccount = serviceAccountService.findById(serviceAccountId)
    			.orElseThrow(() -> accountNotFound(serviceAccountId));
		verifyProjectId(projectId, serviceAccount.getProjectId());
		
		if (restServiceAccountKeyPair.getPassphrase() == null) {
    		throw passphraseNotSet(serviceAccount.getName());
    	}
        
    	ServiceAccount updatedAccount = serviceAccountService
    			.activateNewKey(
    					serviceAccount, 
    					keyPairMapper.convertFromRestServiceAccountKeyPair(restServiceAccountKeyPair),
    					restServiceAccountKeyPair.getPassphrase().toCharArray());
    	
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{serviceAccountId}/key")
                .buildAndExpand(serviceAccountId)
                .toUri();
        return ResponseEntity.created(location)
        		.body(keyPairMapper.convertToRestKeyPair(((KeyPair) updatedAccount.getActiveKeyPair())));
	}

	@Override
    @PermissionCheck(permissions = Permission.WRITE)
	public ResponseEntity<Void> deleteServiceAccount(UUID projectId, UUID serviceAccountId) {
		ServiceAccount serviceAccount = serviceAccountService.findById(serviceAccountId)
    			.orElseThrow(() -> accountNotFound(serviceAccountId));
		verifyProjectId(projectId, serviceAccount.getProjectId());
		serviceAccountService.delete(serviceAccount);
		return ResponseEntity.noContent().build();
	}

	@Override
    @PermissionCheck(permissions = Permission.READ)
	public ResponseEntity<RestServiceAccount> getServiceAccountById(UUID projectId, UUID serviceAccountId) {
		ServiceAccount sa = serviceAccountService.findById(serviceAccountId)
				.orElseThrow(() -> accountNotFound(serviceAccountId));
		verifyProjectId(projectId, sa.getProjectId());
		return ResponseEntity.ok(accountMapper.convertToRestServiceAccount(sa));
	}

	@Override
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<RestKeyPair> getServiceAccountKey() {
        return accountSecurityContext.getAuthenticatedAccount()
                .map(Account::getActiveKeyPair).filter(Objects::nonNull)
                .map(keyPair -> (KeyPair) keyPair)
                .map(keyPairMapper::convertToRestKeyPair)
                .map(ResponseEntity::ok).orElseThrow(this::keyNotFound);
	}

	@Override
    @PermissionCheck(permissions = Permission.WRITE)
	public ResponseEntity<RestKeyPair> getServiceAccountKeyById(UUID projectId, UUID serviceAccountId) {
		return serviceAccountService.findById(serviceAccountId)
				.filter(a -> projectId.equals(a.getProjectId()))
                .flatMap(account -> Optional.ofNullable(account.getActiveKeyPair()))
                .map(keyPairMapper::convertToRestKeyPair)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> keyNotFound(serviceAccountId));
	}

    private ResponseStatusException accountNotFound(UUID accountId) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "no service account with id : " + accountId + " found");
    }
    
    private ResponseStatusException passphraseNotSet(String serviceAccountName) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Passphrase not available on request for sa [%s]", serviceAccountName));
    }

    private ResponseStatusException keyNotFound() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "no active service account key found");
    }
    
    private ResponseStatusException keyNotFound(UUID accountId) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "no active service account key with id : " + accountId.toString() + " found");
    }
    
    private void verifyProjectId(UUID projectId, UUID serviceAccountProjectId) {
		if (!projectId.equals(serviceAccountProjectId)) {
			 throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "projectIds not equal");
			}
    	
    }

}
