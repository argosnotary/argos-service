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
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.stereotype.Service;

import com.argosnotary.argos.domain.ArgosError;
import com.argosnotary.argos.domain.account.ServiceAccount;

@Service
public class ClientRegistrationServiceImpl implements ClientRegistrationService {
	
	public ClientRegistrationServiceImpl(@Autowired ClientRegistrationRepository clientRegistrationRepository) {
		this.clientRegistrationsNameMap = this.getclientRegistrationsNameMap(clientRegistrationRepository);
		this.clientRegistrationsIssuerMap = this.getclientRegistrationsIssuerMap();
		this.issuers =  clientRegistrationsNameMap.values().stream()
				.map(reg -> reg.getProviderDetails().getIssuerUri())
				.collect(Collectors.toSet());
		this.externalProviders =  clientRegistrationsNameMap.keySet().stream()
				.filter(reg -> !internalProviders.contains(reg))
				.map(OAuthProvider::new)
				.toList();
		this.serviceAccountIssuer = clientRegistrationsNameMap.get(ServiceAccount.SA_PROVIDER_NAME).getProviderDetails().getIssuerUri();
	}
	
	private static final Set<String> internalProviders = Set.of("master", "saprovider");
	
	private final Map<String, ClientRegistration> clientRegistrationsNameMap;
	
	private final Map<String, ClientRegistration> clientRegistrationsIssuerMap;
	
	private final Set<String> issuers;
	
	private final List<OAuthProvider> externalProviders;
	
	private final String serviceAccountIssuer;
	
	@Override
	public Optional<String> getClientRegistrationNameWithIssuer(String issuer) {
		if ( clientRegistrationsIssuerMap.containsKey(issuer)) {
			return Optional.of(clientRegistrationsIssuerMap.get(issuer).getRegistrationId());
		}
		return Optional.empty();
	}

	@Override
	public Optional<ClientRegistration> getClientRegistration(String providerName) {
		return clientRegistrationsNameMap.get(providerName) == null? Optional.empty(): Optional.of(clientRegistrationsNameMap.get(providerName));
	}
	
	@Override
	public Optional<String> getClientRegistrationProviderUrl(String providerName) {
		ClientRegistration reg = clientRegistrationsNameMap.get(providerName);
		if (reg == null) {
			return Optional.empty();
		}
		URL url = null;
		try {
			url = new URL(reg.getProviderDetails().getIssuerUri());
		} catch (MalformedURLException e) {
			throw new ArgosError(e.getMessage());
		}
		int urllength = reg.getProviderDetails().getIssuerUri().length();
		int pathLength = url.getPath().length();
		String urlStr = reg.getProviderDetails().getIssuerUri().substring(0, urllength-pathLength);
		return Optional.of(urlStr);
	}

	@Override
	public Set<String> getAllOauthIssuers() {
		return issuers;
	}

	@Override
	public boolean exists(String providerName) {
		return this.getClientRegistration(providerName).isPresent();
	}

	@Override
	public List<OAuthProvider> getAllExternalProviderIds() {
		return externalProviders;
	}
	
	@Override
	public String getServiceAccountIssuer() {
		return this.serviceAccountIssuer;
	}
	
	private Map<String, ClientRegistration> getclientRegistrationsNameMap(ClientRegistrationRepository clientRegistrationRepository) {
		ConcurrentHashMap<String, ClientRegistration> regsMap = new ConcurrentHashMap<>();
		((InMemoryClientRegistrationRepository) clientRegistrationRepository).iterator().forEachRemaining(reg -> regsMap.put(reg.getRegistrationId(), reg));
		return regsMap;		
	}
	
	private Map<String, ClientRegistration> getclientRegistrationsIssuerMap() {
		ConcurrentHashMap<String, ClientRegistration> regsMap = new ConcurrentHashMap<>();
		clientRegistrationsNameMap.values().iterator().forEachRemaining(reg -> regsMap.put(reg.getProviderDetails().getIssuerUri(), reg));
		return regsMap;		
	}
}
