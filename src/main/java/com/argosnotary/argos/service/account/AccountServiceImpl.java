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

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.argosnotary.argos.domain.account.Account;
import com.argosnotary.argos.domain.account.PersonalAccount;
import com.argosnotary.argos.domain.account.ServiceAccount;
import com.argosnotary.argos.domain.crypto.KeyPair;
import com.argosnotary.argos.service.mongodb.account.PersonalAccountRepository;
import com.argosnotary.argos.service.mongodb.account.ServiceAccountRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final ServiceAccountRepository serviceAccountRepository;
    private final PersonalAccountRepository personalAccountRepository;
    
    private final ClientRegistrationService clientRegistrationService;
    
    private final ServiceAccountService serviceAccountService;
    
    private final PersonalAccountService personalAccountService;

    @Override
    public boolean keyPairExists(String keyId) {
        return serviceAccountRepository.existsByActiveKey(keyId) ||
                personalAccountRepository.existsByActiveKey(keyId);
    }

    @Override
    public Optional<KeyPair> findKeyPairByKeyId(String keyId) {
        return serviceAccountRepository
                .findFirstByActiveKeyId(keyId).map(Account.class::cast)
                .or(() -> personalAccountRepository.findFirstByActiveKeyId(keyId)).map(Account::getActiveKeyPair);
    }

	@Override
	public List<Account> findByKeyIds(Set<String> keyIds) {
		List<Account> accounts = personalAccountRepository.findByKeyIds(keyIds);
		accounts.addAll(serviceAccountRepository.findByKeyIds(keyIds));
		return accounts;
	}

	@Override
	public Optional<Account> loadAuthenticatedUser(String providerIssuer, String providerSubject) {
		Optional<String> optProviderName = clientRegistrationService.getClientRegistrationNameWithIssuer(providerIssuer);
		if (optProviderName.isEmpty()) {
			log.warn("Unknown provider used, issuer: [%s], subject: [%s]", providerIssuer, providerSubject);
			return Optional.empty();
		}
		if (clientRegistrationService.getServiceAccountIssuer().equals(providerIssuer)) {
			Optional<ServiceAccount> serviceAccount = serviceAccountService.findByProviderSubject(providerSubject);
			if (serviceAccount.isEmpty()) {
				return Optional.empty();
			} else {
				return Optional.of(serviceAccount.get());
			}
		}

		Optional<PersonalAccount> optPersonalAccount = personalAccountService
				.findByProviderNameAndProviderSubject(optProviderName.get(), providerSubject);
		if (optPersonalAccount.isEmpty()) {
			return Optional.empty();
		}
		return Optional.of(optPersonalAccount.get());
	}

}
