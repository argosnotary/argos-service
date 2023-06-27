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
	
	@Query(value="{'activeKeyPair.keyId': ?0}")
	public Optional<ServiceAccount> findFirstByActiveKeyId(String keyId);

	public List<ServiceAccount> findByProjectId(UUID projectId);
	
	@Query(value="{$or: [{'activeKeyPair.keyId': {$in: ?0}}, {'inActiveKeyPair.keyId': {$in: ?0}}]}")
	public List<Account> findByKeyIds(Set<String> keyIds);
    
    public void deleteByProjectId(UUID projectId);
}
