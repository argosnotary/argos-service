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
package com.argosnotary.argos.service.rest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Set;
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
		
		rkp = keyPairMapper.convertToRestKeyPair(kp);
		
		
    	sa = new RestServiceAccount();
    	sa.setProjectId(UUID.randomUUID());
    	sa.setName("sa");
        
        rskp = new RestServiceAccountKeyPair()
			.keyId(kp.getKeyId())
	        .passphrase("test")
	        .encryptedPrivateKey(kp.getEncryptedPrivateKey())
	        .pub(kp.getPub());
        
        
		
	}

	@Test
	void test() {
		KeyPair keyPair = keyPairMapper.convertFromRestServiceAccountKeyPair(rskp);
		assertEquals(kp, keyPair);
		

	    Set<KeyPair> ks = keyPairMapper.restKeyPairListToKeyPairSet(List.of(rkp));
        assertThat(ks, hasSize(1));
    	assertEquals(kp, ks.iterator().next());
	}

}
