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
package com.argosnotary.argos.domain.account;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.argosnotary.argos.domain.account.PersonalAccount.Profile;
import com.argosnotary.argos.domain.crypto.KeyPair;
import com.argosnotary.argos.domain.roles.Role;
import com.argosnotary.argos.domain.roles.Role.Releaser;

@ExtendWith(MockitoExtension.class)
class PersonalAccountTest {

    private static final String EMAIL = "email";
    private static final String EMAIL2 = "email2";
    private static final String NAME = "name";
    private static final String NAME2 = "name2";
    private static final String PROVIDER_NAME = "providerName";
    private static final String PROVIDER_SUBJECT = "providerSubject";
    private static final Role ROLE = new Releaser();
    private static final UUID RESOURCE_ID = UUID.randomUUID();
    protected static final String AZURE = "azure";

    @Mock
    private KeyPair activeKeyPair;

    @Mock
    private KeyPair keyPair;

    @Test
    void builder() {
        Set<Role> roles = new HashSet<>();
        roles.add(ROLE);
        Profile profile = Profile.builder()
                .email(EMAIL)
                .build();
        PersonalAccount account = PersonalAccount.builder().name(NAME)
        		.profile(profile)
                .activeKeyPair(activeKeyPair)
                .inactiveKeyPairs(Collections.singleton(keyPair))
                .providerName(AZURE)
                .providerName(PROVIDER_NAME)
                .providerSubject(PROVIDER_SUBJECT)
                .build();
        assertThat(account.getName(), is(NAME));
        assertThat(account.getActiveKeyPair(), sameInstance(activeKeyPair));
        assertThat(account.getProviderName(), is(PROVIDER_NAME));
        assertThat(account.getProviderSubject(), is(PROVIDER_SUBJECT));
        assertThat(account.getInactiveKeyPairs(), contains(keyPair));
        assertThat(account.getProfile(), is(profile));
        
        PersonalAccount account2 = PersonalAccount.builder()
                .build();
    }
    
    @Test
    void setterTest() {
        Profile profile = Profile.builder().build();
        profile.setEmail(EMAIL);
        PersonalAccount account = PersonalAccount.builder().build();
        account.setName(NAME);
        account.setProfile(profile);
        account.setActiveKeyPair(activeKeyPair);
        account.setInactiveKeyPairs(Collections.singleton(keyPair));
        account.setProviderName(AZURE);
        account.setProviderName(PROVIDER_NAME);
        account.setProviderSubject(PROVIDER_SUBJECT);     


        assertThat(account.getName(), is(NAME));
        assertThat(account.getProfile().getEmail(), is(EMAIL));
        assertThat(account.getActiveKeyPair(), sameInstance(activeKeyPair));
        assertThat(account.getProviderName(), is(PROVIDER_NAME));
        assertThat(account.getProviderSubject(), is(PROVIDER_SUBJECT));
        assertThat(account.getInactiveKeyPairs(), contains(keyPair));
        assertThat(account.getProfile(), is(profile));
    }
    
    @Test
    void testIsForOidcEqual() {
        Profile profile = Profile.builder()
                .email(EMAIL)
                .build();
        
    	PersonalAccount account = PersonalAccount.builder().build();
        account.setName(NAME);
        account.setProfile(profile);
        account.setActiveKeyPair(activeKeyPair);
        account.setInactiveKeyPairs(Collections.singleton(keyPair));
        account.setProviderName(AZURE);
        account.setProviderName(PROVIDER_NAME);
        account.setProviderSubject(PROVIDER_SUBJECT);
        

        Profile profile2 = Profile.builder()
                .email(EMAIL)
                .build();
        PersonalAccount account2 = PersonalAccount.builder().build();
        account2.setName(NAME);
        account2.setProfile(profile2);
        account2.setActiveKeyPair(activeKeyPair);
        account2.setInactiveKeyPairs(Collections.singleton(keyPair));
        account2.setProviderName(AZURE);
        account2.setProviderName(PROVIDER_NAME);
        account2.setProviderSubject(PROVIDER_SUBJECT);
        
        assertTrue(account.getProfile().equals(account2.getProfile()));
    }
    
    @Test
    void testIsForOidcNotEqual() {
        Profile profile = Profile.builder()
                .email(EMAIL)
                .build();
    	PersonalAccount account = PersonalAccount.builder().build();
        account.setName(NAME);
        account.setProfile(profile);
        account.setActiveKeyPair(activeKeyPair);
        account.setInactiveKeyPairs(Collections.singleton(keyPair));
        account.setProviderName(AZURE);
        account.setProviderName(PROVIDER_NAME);
        account.setProviderSubject(PROVIDER_SUBJECT);

        

        Profile profile2 = Profile.builder()
                .email(EMAIL2)
                .build();
        
        PersonalAccount account2 = PersonalAccount.builder().build();
        account2.setName(NAME2);
        account2.setProfile(profile2);
        account2.setActiveKeyPair(activeKeyPair);
        account2.setInactiveKeyPairs(Collections.singleton(keyPair));
        account2.setProviderName(AZURE);
        account2.setProviderName(PROVIDER_NAME);
        account2.setProviderSubject(PROVIDER_SUBJECT);
        
        assertFalse(account.getProfile().equals(account2.getProfile()));
    }
}