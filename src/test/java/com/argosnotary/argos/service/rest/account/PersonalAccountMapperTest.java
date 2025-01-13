/*
 * Argos Notary - A new way to secure the Software Supply Chain
 *
 * Copyright (C) 2019 - 2020 Rabobank Nederland
 * Copyright (C) 2019 - 2025 Gerard Borst <gerard.borst@argosnotary.com>
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
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.util.io.pem.PemGenerationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.argosnotary.argos.domain.account.PersonalAccount;
import com.argosnotary.argos.domain.account.PersonalAccount.Profile;
import com.argosnotary.argos.domain.crypto.CryptoHelper;
import com.argosnotary.argos.domain.crypto.KeyPair;
import com.argosnotary.argos.service.openapi.rest.model.RestKeyPair;
import com.argosnotary.argos.service.openapi.rest.model.RestPersonalAccount;
import com.argosnotary.argos.service.openapi.rest.model.RestProfile;
import com.argosnotary.argos.service.rest.KeyPairMapper;
import com.argosnotary.argos.service.rest.KeyPairMapperImpl;

@SpringBootTest(classes= {PersonalAccountMapperImpl.class, KeyPairMapperImpl.class})
class PersonalAccountMapperTest {

    private static final String EMAIL = "email";
    private static final String NAME = "name";
    private static final UUID LABEL_ID = UUID.randomUUID();
    private static final char[] PRIVAT_KEY_PASSPHRASE = "test".toCharArray();

    @Autowired
    private PersonalAccountMapper mapper;

    @Autowired
    private KeyPairMapper keyPairMapper;
    
    private RestKeyPair restKeyPair;

    private KeyPair keyPair;
    
    private PersonalAccount personalAccount;

    @BeforeEach
    void setUp() throws NoSuchAlgorithmException, OperatorCreationException, PemGenerationException, MalformedURLException {
        //keyPairMapper = Mappers.getMapper(KeyPairMapper.class);
        
        keyPair = CryptoHelper.createKeyPair(PRIVAT_KEY_PASSPHRASE);
        restKeyPair = keyPairMapper.convertToRestKeyPair(keyPair);
        personalAccount = PersonalAccount.builder()
        		.activeKeyPair(keyPair)
        		.profile(Profile.builder().email(EMAIL).fullName("Luke Skywalker").givenName("luke").familyName("Skywalker").picture(new URL("http://localhost/picture")).build())
        		.providerName("oauth-stub")
        		.providerSubject("subject")
        		.name("luke")
        		.inactiveKeyPairs(Set.of(keyPair))
        		.build();
    }
    
    @Test
    void convertToRestPersonalAccount() {
        RestPersonalAccount restPersonalAccount = mapper.convertToRestPersonalAccount(personalAccount);
        validate(restPersonalAccount);
        assertEquals(personalAccount.getId(), restPersonalAccount.getId() );
        assertEquals(personalAccount.getProviderSubject(), restPersonalAccount.getProviderSubject() );
        assertEquals(personalAccount.getProviderName(), restPersonalAccount.getProviderName() );
        assertEquals(personalAccount.getName(), restPersonalAccount.getName() );
        assertEquals(personalAccount.getProfile().getFullName(), restPersonalAccount.getProfile().getFullName() );
        assertEquals(personalAccount.getProfile().getGivenName(), restPersonalAccount.getProfile().getGivenName() );
        assertEquals(personalAccount.getProfile().getFamilyName(), restPersonalAccount.getProfile().getFamilyName() );
        assertEquals(personalAccount.getProfile().getEmail(), restPersonalAccount.getProfile().getEmail() );
        assertEquals(personalAccount.getProfile().getPicture().toString(), restPersonalAccount.getProfile().getPicture() );
        assertEquals(personalAccount.getActiveKeyPair(),keyPairMapper.convertFromRestKeyPair(restPersonalAccount.getActiveKeyPair()) );
        assertThat(restPersonalAccount.getInactiveKeyPairs(), contains(restKeyPair) );
    }
    
    @Test
    void testInactiveKeyListNullOrEmpty() {
    	personalAccount.setInactiveKeyPairs(null);
        RestPersonalAccount restPersonalAccount = mapper.convertToRestPersonalAccountIdentity(personalAccount);
        assertEquals(null, restPersonalAccount.getInactiveKeyPairs());

    	personalAccount.setInactiveKeyPairs(Set.of());
        restPersonalAccount = mapper.convertToRestPersonalAccountIdentity(personalAccount);
        assertThat(restPersonalAccount.getInactiveKeyPairs().size(), is(0));
    }
    
    @Test
    void convertToRestPersonalAccountIdentity() {
        RestPersonalAccount restPersonalAccount = mapper.convertToRestPersonalAccountIdentity(personalAccount);
        validate(restPersonalAccount);
        assertThat(restPersonalAccount.getProfile(), nullValue());
        assertEquals(personalAccount.getId(), restPersonalAccount.getId() );
        assertEquals(personalAccount.getProviderSubject(), restPersonalAccount.getProviderSubject() );
        assertEquals(personalAccount.getProviderName(), restPersonalAccount.getProviderName() );
        assertEquals(personalAccount.getName(), restPersonalAccount.getName() );
        assertEquals(personalAccount.getActiveKeyPair(),keyPairMapper.convertFromRestKeyPair(restPersonalAccount.getActiveKeyPair()) );
        assertThat(restPersonalAccount.getInactiveKeyPairs(), contains(restKeyPair) );
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