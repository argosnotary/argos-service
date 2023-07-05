/*
 * Argos Notary - A new way to secure the Software Supply Chain
 *
 * Copyright (C) 2019 - 2020 Rabobank Nederland
 * Copyright (C) 2019 - 2022 Gerard Borst <gerard.borst@argosnotary.com>
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

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.argosnotary.argos.domain.account.ServiceAccount;
import com.argosnotary.argos.domain.crypto.KeyPair;
import com.argosnotary.argos.service.mongodb.account.ServiceAccountRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ServiceAccountServiceImpl implements ServiceAccountService {

    private final ServiceAccountRepository serviceAccountRepository;
    
    private final ServiceAccountProviderService serviceAccountProviderService;

    @Override
    public void delete(ServiceAccount account) {
    	serviceAccountProviderService.unRegisterServiceAccount(account);
        serviceAccountRepository.deleteById(account.getId());
    }

    @Override
    public Optional<ServiceAccount> findById(UUID accountId) {
        return serviceAccountRepository.findById(accountId);
    }
    
    @Override
    public ServiceAccount activateNewKey(ServiceAccount serviceAccount, KeyPair newKeyPair, char[] passphrase) {
    	serviceAccount.deactivateKeyPair(newKeyPair);
		// change password in sa provider
    	serviceAccountProviderService.setServiceAccountPassword(serviceAccount, passphrase);
    	return serviceAccountRepository.save(serviceAccount);
    }

	@Override
	public ServiceAccount createServiceAccount(ServiceAccount serviceAccount) {
        serviceAccountProviderService.registerServiceAccount(serviceAccount);
		return serviceAccountRepository.insert(serviceAccount);
	}

	@Override
	public boolean exists(UUID accountId) {
		return serviceAccountRepository.existsById(accountId);
	}

	@Override
	public boolean exists(UUID projectId, String name) {
		return serviceAccountRepository.existsByProjectIdAndName(projectId, name);
	}

}
