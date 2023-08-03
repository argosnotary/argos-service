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
package com.argosnotary.argos.service.account;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.argosnotary.argos.domain.ArgosError;
import com.argosnotary.argos.domain.account.ServiceAccount;

import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ServiceAccountProviderServiceImpl implements ServiceAccountProviderService {
	
	private String tokenClientId;
	
	private String tokenClientSecret;
	
	private RealmResource realmResource;	
	
	private ClientRegistrationService clientRegistrationService;
	
	private Keycloak keycloak;

    public ServiceAccountProviderServiceImpl(
    		ClientRegistrationService clientRegistrationService,
    		@Value("${keycloak.admin-client.client-id}") String clientId, 
    		@Value("${keycloak.admin-client.client-secret}") String clientSecret,
    		@Value("${keycloak.token-client.client-id}") String tokenClientId, 
    		@Value("${keycloak.token-client.client-secret}") String tokenClientSecret) {
    	if (!clientRegistrationService.exists(ServiceAccount.SA_PROVIDER_NAME)) {
			throw new ArgosError(String.format("OIDC provider definition of [%s] doesn't exist", ServiceAccount.SA_PROVIDER_NAME));
		}
    	this.clientRegistrationService = clientRegistrationService;
    	this.tokenClientId = tokenClientId;
    	this.tokenClientSecret = tokenClientSecret;
		keycloak = KeycloakBuilder.builder()
	            .serverUrl(clientRegistrationService.getClientRegistrationProviderUrl(ServiceAccount.SA_PROVIDER_NAME).orElseThrow())
	            .clientId(clientId)
	            .clientSecret(clientSecret)
	            .realm(ServiceAccount.SA_PROVIDER_NAME)
	            .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
	            .build();
	    realmResource = keycloak.realm(ServiceAccount.SA_PROVIDER_NAME);
    }

	@Override
	public ServiceAccount registerServiceAccount(ServiceAccount sa) {
		UserRepresentation user = new UserRepresentation();
        user.setEnabled(true);
        user.setUsername(sa.getId().toString());
        user.setFirstName(sa.getName());

        // Get realm
        UsersResource userResource = realmResource.users();

        // Create user (requires manage-users role)
        Response response = userResource.create(user);
        if (response.getStatus() == 201) {
        	String[] parts = response.getLocation().getPath().split("/");
        	String subject = parts[parts.length-1];            
            sa.setProviderSubject(subject);
        	log.info("User created with name: [{}], accountId: [{}] and subject: [{}] ", user.getUsername(), user.getFirstName(), sa.getProviderSubject());
        }
        return sa;

	}

	@Override
	public void unRegisterServiceAccount(ServiceAccount sa) {
		Optional<UserRepresentation> optUser = getUser(sa.getId());
		if (optUser.isPresent()) {
			Response response = realmResource.users().delete(optUser.get().getId());
	        if (response.getStatus() != 204) {
	        	log.info("User unregister with name: [{}], accountId: [{}] and subject: [{}]  failed", optUser.get().getUsername(), optUser.get().getFirstName(), sa.getProviderSubject());	        	
	        }
		}
	}

	@Override
	public void setServiceAccountPassword(ServiceAccount sa, char[] passphrase) {
		// Define password credential
        CredentialRepresentation passwordCred = new CredentialRepresentation();
        passwordCred.setTemporary(false);
        passwordCred.setType(CredentialRepresentation.PASSWORD);
        passwordCred.setValue(new String(passphrase));

        // Set password credential
        Optional<UserRepresentation> optUser = getUser(sa.getId());
		if (optUser.isEmpty()) {
			throw new ArgosError(String.format("Service Account [%s] not found in saprovider", sa.getId().toString()));
		}
		getUser(optUser.get()).resetPassword(passwordCred);
	}

	@Override
	public boolean exists(UUID id) {
		Optional<UserRepresentation> optUser = getUser(id);
		return optUser.isPresent();
	}

	@Override
	public String getIdToken(UUID id, char[] password) {
		Optional<String> ff = clientRegistrationService.getClientRegistrationProviderUrl(ServiceAccount.SA_PROVIDER_NAME);
		return KeycloakBuilder.builder()
				.serverUrl(clientRegistrationService.getClientRegistrationProviderUrl(ServiceAccount.SA_PROVIDER_NAME).orElseThrow())
		        .clientId(tokenClientId)
		        .clientSecret(tokenClientSecret)
		        .realm(ServiceAccount.SA_PROVIDER_NAME)
		        .username(id.toString())
		        .password(String.valueOf(password))
		        .grantType(OAuth2Constants.PASSWORD)
		        .scope("openid")
		        .build()
		        .tokenManager().getAccessToken().getIdToken();
	}
	
	private Optional<UserRepresentation> getUser(UUID accountId) {
		List<UserRepresentation> users = realmResource.users().searchByUsername(accountId.toString(), true);
		if (users.isEmpty()) {
			return Optional.empty();
		}
		return Optional.of(users.get(0));
	}
	
	private UserResource getUser(UserRepresentation keycloakUser) {
		return realmResource.users().get(keycloakUser.getId());
	}
}
