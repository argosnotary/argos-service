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

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.argosnotary.argos.domain.ArgosError;
import com.argosnotary.argos.domain.account.ServiceAccount;
import com.argosnotary.argos.service.mongodb.account.ServiceAccountRepository;

import jakarta.annotation.PostConstruct;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ServiceAccountProviderServiceImpl implements ServiceAccountProviderService {
	
	@Value("${keycloak.admin-client.realm}")
	private String realm;
	
	@Value("${keycloak.admin-client.client-id}")
	private String clientId;
	
	@Value("${keycloak.admin-client.client-secret}")
	private String clientSecret;
	
	@Value("${keycloak.admin-client.grant-type}")
	private String grantType;
	
	private RealmResource realmResource;	
	
	private final ClientRegistrationService clientRegistrationService;
	
	private Keycloak keycloak;

    @PostConstruct
    public void initKeycloak() {
		if (!clientRegistrationService.exists(ServiceAccount.SA_PROVIDER_NAME)) {
			throw new ArgosError(String.format("OIDC provider definition of [%] doesn't exist", ServiceAccount.SA_PROVIDER_NAME));
		}
		keycloak = KeycloakBuilder.builder()
	            .serverUrl(clientRegistrationService.getClientRegistrationProviderUrl(ServiceAccount.SA_PROVIDER_NAME).get())
	            .clientId(clientId)
	            .clientSecret(clientSecret)
	            .realm(realm)
	            .grantType(grantType)
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
        UsersResource userRessource = realmResource.users();

        // Create user (requires manage-users role)
        Response response = userRessource.create(user);
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
			getUser(optUser.get()).remove();
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
	public URL getProviderIssuer() {
		try {
			return new URL(clientRegistrationService.getClientRegistration(ServiceAccount.SA_PROVIDER_NAME).get().getProviderDetails().getIssuerUri());
		} catch (MalformedURLException e) {
			throw new ArgosError(e.getMessage());
		}
	}

	@Override
	public boolean exists(ServiceAccount sa) {
		Optional<UserRepresentation> optUser = getUser(sa.getId());
		if (optUser.isEmpty()) {
			return false;
		}
		return true;
	}
	


	@Override
	public boolean isProviderIssuer(String issuer) {
		return getProviderIssuer().toString().equals(issuer);
	}
	
	private Optional<UserRepresentation> getUser(UUID accountId) {
		List<UserRepresentation> users = realmResource.users().search(accountId.toString());
		if (users.isEmpty()) {
			return Optional.empty();
		}
		return Optional.of(users.get(0));
	}
	
	private UserResource getUser(UserRepresentation keycloakUser) {
		return realmResource.users().get(keycloakUser.getId());
	}
}
