package com.argosnotary.argos.service.mongodb.account;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.argosnotary.argos.domain.account.Account;
import com.argosnotary.argos.domain.account.PersonalAccount;

public interface PersonalAccountRepository extends MongoRepository<PersonalAccount, UUID> {

	@Query(value="{'activeKeyPair.keyId': ?0}", exists=true)
	public Boolean existsByActiveKey(String keyId);
	
	@Query(value="{'activeKeyPair.keyId': ?0}", fields="{'profile': 0, 'activeKeyPair.encryptedPrivateKey': 0, 'inActiveKeyPair.encryptedPrivateKey': 0}")
	public Optional<PersonalAccount> findFirstByActiveKeyId(String keyId);
	
	@Query(value="{$or: [{'activeKeyPair.keyId': {$in: ?0}}, {'inActiveKeyPair.keyId': {$in: ?0}}]}", fields="{'profile': 0, 'activeKeyPair.encryptedPrivateKey': 0, 'inActiveKeyPair.encryptedPrivateKey': 0}")
	public List<Account> findByKeyIds(Set<String> keyIds);
	
	public Optional<PersonalAccount> findFirstByProviderNameAndProviderSubject(String providerName, String providerSubject);
}