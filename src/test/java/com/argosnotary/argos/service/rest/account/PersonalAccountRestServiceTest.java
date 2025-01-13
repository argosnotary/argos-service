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
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.util.io.pem.PemGenerationException;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import com.argosnotary.argos.domain.account.PersonalAccount;
import com.argosnotary.argos.domain.account.PersonalAccount.Profile;
import com.argosnotary.argos.domain.crypto.CryptoHelper;
import com.argosnotary.argos.domain.crypto.KeyPair;
// import com.argosnotary.argos.service.account.AccountSearchParams;
import com.argosnotary.argos.service.account.AccountSecurityContext;
import com.argosnotary.argos.service.account.PersonalAccountService;
import com.argosnotary.argos.service.openapi.rest.model.RestKeyPair;
import com.argosnotary.argos.service.openapi.rest.model.RestPersonalAccount;
import com.argosnotary.argos.service.openapi.rest.model.RestPublicKey;
import com.argosnotary.argos.service.rest.KeyPairMapper;
import com.argosnotary.argos.service.rest.KeyPairMapperImpl;

@SpringBootTest(classes= {PersonalAccountMapperImpl.class, KeyPairMapperImpl.class})
class PersonalAccountRestServiceTest {

    private static final String USER_NAME = "accountName";
    private static final String PERSONAL_ACCOUNT_NOT_FOUND = "404 NOT_FOUND \"personal account not found\"";
    public static final String ACTIVE_KEYPAIR_NOT_FOUND = "404 NOT_FOUND \"no active keypair found for account: accountName\"";
    private static final UUID ACCOUNT_ID = UUID.randomUUID();
    private static final UUID LABEL_ID = UUID.randomUUID();
    private static final UUID OTHER_LABEL_ID = UUID.randomUUID();
    private static final String KEY1 = "key1";
    private static final String KEY2 = "key2";
    private static final char[] PRIVAT_KEY_PASSPHRASE = "test".toCharArray();
    private static final String SESSION_ID = "sessionId";
    private static final Date EXPIRATION_DATE = new Date();

    private PersonalAccountRestService service;
    
    @Mock
    private AccountSecurityContext accountSecurityContext;

    @Autowired
    private KeyPairMapper keyPairMapper;

    @Autowired
    private PersonalAccountMapper personalAccountMapper;

    private RestPersonalAccount restPersonalAccount;

    private RestPersonalAccount restPAOnlyIdentity;
    
    private RestKeyPair restKeyPair;

    private KeyPair keyPair;
    
    private RestPublicKey restPublicKey;

    private PersonalAccount personalAccount;

    @Mock
    private PersonalAccountService accountService;

//    @Captor
//    private ArgumentCaptor<AccountSearchParams> searchParamsArgumentCaptor;

    @BeforeEach
    void setUp() throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, OperatorCreationException, PemGenerationException {
    	keyPair = CryptoHelper.createKeyPair(PRIVAT_KEY_PASSPHRASE);
        restKeyPair = keyPairMapper.convertToRestKeyPair(keyPair);
        restPublicKey = new RestPublicKey(restKeyPair.getKeyId(), restKeyPair.getPub());
        personalAccount = PersonalAccount.builder()
        		.name(USER_NAME)
        		.id(ACCOUNT_ID)
        		.activeKeyPair(keyPair)
        		.providerName("oauth-stub")
        		.profile(Profile.builder().fullName("Luke Skywalker").build())
        		.build();
        restPersonalAccount = personalAccountMapper.convertToRestPersonalAccount(personalAccount);
        restPAOnlyIdentity = personalAccountMapper.convertToRestPersonalAccountIdentity(personalAccount);

        service = new PersonalAccountRestServiceImpl(accountSecurityContext, keyPairMapper, accountService, personalAccountMapper);
    }

    @Test
    void getCurrentUserNotFound() {
        when(accountSecurityContext.getAuthenticatedAccount()).thenReturn(Optional.empty());
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
        	service.whoAmI();
          });
        
        assertEquals("404 NOT_FOUND \"personal account not found\"", exception.getMessage());
        assertThat(exception.getStatusCode().value(), is(404));
        assertThat(exception.getMessage(), is(PERSONAL_ACCOUNT_NOT_FOUND));
    }

    @Test
    void whoAmI() {
        when(accountSecurityContext.getAuthenticatedAccount()).thenReturn(Optional.of(personalAccount));
        ResponseEntity<RestPersonalAccount> responseEntity = service.whoAmI();
        assertThat(responseEntity.getStatusCode().value(), Matchers.is(200));
        RestPersonalAccount restPersonalAccount = responseEntity.getBody();
    }

    @Test
    void storeKeyShouldReturnSuccess() {
        when(accountSecurityContext.getAuthenticatedAccount()).thenReturn(Optional.of(personalAccount));
        assertThat(service.createKey(restKeyPair).getStatusCode().value(), is(204));
        verify(accountService).activateNewKey(personalAccount, keyPair);
    }

    @Test
    void storeInvalidKeyShouldReturnException() {
    	restKeyPair.setKeyId("invalid");
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> { 
        	service.createKey(restKeyPair);
        });
        assertEquals(String.format("400 BAD_REQUEST \"invalid key id : invalid\"", restKeyPair.getKeyId()), exception.getMessage());
    }

    @Test
    void storeKeyShouldReturnBadRequest() {
        RestKeyPair rkp = new RestKeyPair(keyPair.getEncryptedPrivateKey(), "incorrect key", keyPair.getPub());
        restKeyPair = keyPairMapper.convertToRestKeyPair(keyPairMapper.convertFromRestKeyPair(rkp));
        assertThrows(ResponseStatusException.class, () -> service.createKey(restKeyPair));
    }

    @Test
    void storeKeyShouldReturnNotFound() {
        when(accountSecurityContext.getAuthenticatedAccount()).thenReturn(Optional.empty());
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
        	service.createKey(restKeyPair);
          });
        
        assertEquals("404 NOT_FOUND \"personal account not found\"", exception.getMessage());
        assertThat(exception.getStatusCode().value(), is(404));
        assertThat(exception.getMessage(), is(PERSONAL_ACCOUNT_NOT_FOUND));
    }

    @Test
    void getKeyPairShouldReturnOK() {
        when(accountSecurityContext.getAuthenticatedAccount()).thenReturn(Optional.of(personalAccount));
        personalAccount.setActiveKeyPair(keyPair);
        ResponseEntity<RestKeyPair> responseEntity = service.getKeyPair();
        assertThat(responseEntity.getStatusCode().value(), Matchers.is(200));
        assertThat(responseEntity.getBody(), is(restKeyPair));
    }

    @Test
    void getKeyPairShouldReturnNotFound() {
        when(accountSecurityContext.getAuthenticatedAccount()).thenReturn(Optional.of(personalAccount));
        personalAccount.setActiveKeyPair(null);
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> service.getKeyPair());
        assertThat(exception.getStatusCode().value(), is(404));
        assertThat(exception.getMessage(), is(ACTIVE_KEYPAIR_NOT_FOUND));
    }

    @Test
    void getPersonalAccountById() {
        when(accountService.getPersonalAccountById(ACCOUNT_ID)).thenReturn(Optional.of(personalAccount));
        ResponseEntity<RestPersonalAccount> response = service.getPersonalAccountById(ACCOUNT_ID);
        assertThat(response.getBody(), is(restPAOnlyIdentity));
        assertThat(response.getStatusCode().value(), Matchers.is(200));
    }

    @Test
    void getPersonalAccountByIdNotFound() {
        when(accountService.getPersonalAccountById(ACCOUNT_ID)).thenReturn(Optional.empty());
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> service.getPersonalAccountById(ACCOUNT_ID));
        assertThat(exception.getStatusCode().value(), is(404));
        assertThat(exception.getMessage(), is(PERSONAL_ACCOUNT_NOT_FOUND));
    }

//    @Test
//    void searchPersonalAccounts() {
//        when(accountService.searchPersonalAccounts(any(AccountSearchParams.class))).thenReturn(List.of(personalAccount));
//        ResponseEntity<List<RestPersonalAccount>> response = service.searchPersonalAccounts(LABEL_ID, USER_NAME, List.of(KEY1), List.of(KEY2));
//        assertThat(response.getBody(), contains(restPAOnlyIdentity));
//        assertThat(response.getStatusCodeValue(), Matchers.is(200));
//        verify(accountService).searchPersonalAccounts(searchParamsArgumentCaptor.capture());
//        AccountSearchParams searchParams = searchParamsArgumentCaptor.getValue();
//        assertThat(searchParams.getLocalPermissionsLabelId(), is(Optional.of(LABEL_ID)));
//        assertThat(searchParams.getRole(), is(Optional.empty()));
//        assertThat(searchParams.getUserName(), is(Optional.of(USER_NAME)));
//        assertThat(searchParams.getActiveKeyIds().get(), contains(KEY1));
//        assertThat(searchParams.getInActiveKeyIds().get(), contains(KEY2));
//    }

    @Test
    void getPersonalAccountKeyById() {
        when(accountService.getPersonalAccountById(ACCOUNT_ID)).thenReturn(Optional.of(personalAccount));
        ResponseEntity<RestPublicKey> response = service.getPersonalAccountKeyById(ACCOUNT_ID);
        assertThat(response.getStatusCode().value(), is(200));
        assertThat(response.getBody(), is(restPublicKey));
    }

    @Test
    void getPersonalAccountKeyByIdNoAccount() {
        when(accountService.getPersonalAccountById(ACCOUNT_ID)).thenReturn(Optional.empty());
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> service.getPersonalAccountKeyById(ACCOUNT_ID));
        assertThat(exception.getMessage(), is("404 NOT_FOUND \"personal account not found\""));
    }

    @Test
    void getPersonalAccountKeyByIdNoActiveKey() {
        when(accountService.getPersonalAccountById(ACCOUNT_ID)).thenReturn(Optional.of(personalAccount));
        personalAccount.setActiveKeyPair(null);
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> service.getPersonalAccountKeyById(ACCOUNT_ID));
        assertThat(exception.getMessage(), is("404 NOT_FOUND \"no active keypair found for account: accountName\""));
    }
    
    
}