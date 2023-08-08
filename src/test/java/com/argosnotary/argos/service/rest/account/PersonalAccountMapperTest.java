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
package com.argosnotary.argos.service.rest.account;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import java.util.UUID;

import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.util.io.pem.PemGenerationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.junit.jupiter.MockitoExtension;

import com.argosnotary.argos.domain.account.PersonalAccount;
import com.argosnotary.argos.domain.account.PersonalAccount.Profile;
import com.argosnotary.argos.domain.crypto.CryptoHelper;
import com.argosnotary.argos.domain.crypto.KeyPair;
import com.argosnotary.argos.service.openapi.rest.model.RestKeyPair;
import com.argosnotary.argos.service.openapi.rest.model.RestPersonalAccount;
import com.argosnotary.argos.service.openapi.rest.model.RestProfile;

@ExtendWith(MockitoExtension.class)
class PersonalAccountMapperTest {

    private static final String EMAIL = "email";
    private static final String NAME = "name";
    private static final UUID LABEL_ID = UUID.randomUUID();
    private static final char[] PRIVAT_KEY_PASSPHRASE = "test".toCharArray();

    private PersonalAccountMapper mapper;

    private KeyPairMapper keyPairMapper;
    
    private RestKeyPair restKeyPair;

    private KeyPair keyPair;
    
    private PersonalAccount personalAccount;

    @BeforeEach
    void setUp() throws NoSuchAlgorithmException, OperatorCreationException, PemGenerationException {
        mapper = Mappers.getMapper(PersonalAccountMapper.class);
        keyPairMapper = Mappers.getMapper(KeyPairMapper.class);
        
        keyPair = CryptoHelper.createKeyPair(PRIVAT_KEY_PASSPHRASE);
        restKeyPair = keyPairMapper.convertToRestKeyPair(keyPair);
        personalAccount = PersonalAccount.builder()
        		.activeKeyPair(keyPair)
        		.profile(Profile.builder().email(EMAIL).fullName("Luke Skywalker").build())
        		.providerName("oauth-stub")
        		.build();
    }
    
    @Test
    void convertToRestPersonalAccount() {
        RestPersonalAccount restPersonalAccount = mapper.convertToRestPersonalAccount(personalAccount);
        validate(restPersonalAccount);
    }
    
    @Test
    void convertToRestPersonalAccountIdentity() {
        RestPersonalAccount restPersonalAccount = mapper.convertToRestPersonalAccountIdentity(personalAccount);
        validate(restPersonalAccount);
        assertThat(restPersonalAccount.getProfile(), nullValue());
    }

    private void validate(RestPersonalAccount restPersonalAccount) {
        assertThat(restPersonalAccount.getName(), is(personalAccount.getName()));
        assertThat(restPersonalAccount.getId(), is(personalAccount.getId()));
    }
    
    @Test
    void testConvertToRestProfile() {
    	Profile prof = Profile.builder().email(EMAIL).familyName(NAME).fullName(NAME).givenName(NAME).build();
    	RestProfile rp = mapper.convertToRestProfile(prof);
    	RestProfile rp2 = mapper.convertToRestProfile(Optional.of(prof));
    	assertEquals(rp, rp2);
    	assertNull(mapper.convertToRestProfile(Optional.empty()));
    	
    }
}