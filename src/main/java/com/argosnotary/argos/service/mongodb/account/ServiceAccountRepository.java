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
package com.argosnotary.argos.service.mongodb.account;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.argosnotary.argos.domain.account.Account;
import com.argosnotary.argos.domain.account.ServiceAccount;

public interface ServiceAccountRepository extends MongoRepository<ServiceAccount, UUID> {
	
	@Query(value="{'activeKeyPair.keyId': ?0}", exists=true)
	public Boolean existsByActiveKey(String keyId);
	
	public Boolean existsByProjectIdAndName(UUID projectId, String name);

	public Optional<ServiceAccount> findFirstByProviderSubject(String providerSubject);
	
	@Query(value="{'activeKeyPair.keyId': ?0}", fields="{'activeKeyPair.encryptedPrivateKey': 0, 'inActiveKeyPair.encryptedPrivateKey': 0}")
	public Optional<ServiceAccount> findFirstByActiveKeyId(String keyId);

	public List<ServiceAccount> findByProjectId(UUID projectId);
	
	@Query(value="{$or: [{'activeKeyPair.keyId': {$in: ?0}}, {'inActiveKeyPair.keyId': {$in: ?0}}]}", fields="{'activeKeyPair.encryptedPrivateKey': 0, 'inActiveKeyPair.encryptedPrivateKey': 0}")
	public List<Account> findByKeyIds(Set<String> keyIds);
    
    public void deleteByProjectId(UUID projectId);
}
