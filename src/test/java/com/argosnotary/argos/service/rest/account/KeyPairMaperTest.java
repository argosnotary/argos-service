package com.argosnotary.argos.service.rest.account;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import com.argosnotary.argos.domain.crypto.CryptoHelper;
import com.argosnotary.argos.domain.crypto.KeyPair;
import com.argosnotary.argos.service.itest.rest.api.model.RestServiceAccount;
import com.argosnotary.argos.service.openapi.rest.model.RestKeyPair;
import com.argosnotary.argos.service.openapi.rest.model.RestServiceAccountKeyPair;

class KeyPairMaperTest {
	
	private KeyPairMapper keyPairMapper;
	
	private RestKeyPair rkp;
	private KeyPair kp;
	RestServiceAccount sa;
	RestServiceAccountKeyPair rskp;

	@BeforeEach
	void setUp() throws Exception {
		keyPairMapper = Mappers.getMapper(KeyPairMapper.class);
		
		kp = CryptoHelper.createKeyPair("test".toCharArray());
		
		
    	sa = new RestServiceAccount();
    	sa.setProjectId(UUID.randomUUID());
    	sa.setName("sa");
        
        rskp = new RestServiceAccountKeyPair()
			.keyId(kp.getKeyId())
	        .passphrase("test")
	        .encryptedPrivateKey(kp.getEncryptedPrivateKey())
	        .publicKey(kp.getPublicKey());
        
        
		
	}

	@Test
	void test() {
		KeyPair keyPair = keyPairMapper.convertFromRestServiceAccountKeyPair(rskp);
		assertEquals(kp, keyPair);
	}

}
