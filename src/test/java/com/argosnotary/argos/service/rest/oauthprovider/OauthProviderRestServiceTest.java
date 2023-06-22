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
package com.argosnotary.argos.service.rest.oauthprovider;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.when;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

import com.argosnotary.argos.service.account.ClientRegistrationService;
import com.argosnotary.argos.service.account.OAuthProvider;
import com.argosnotary.argos.service.openapi.rest.model.RestOAuthProvider;

//@RunWith(SpringRunner.class)
//@SpringBootTest
class OauthProviderRestServiceTest {
    protected static final String DISPLAY_NAME = "displayName";
    protected static final String AZURE = "azure";
    protected static final String ICON_URL = "iconUrl";
    private OAuthProvider oAuthProvider;
    private OAuthProvider azureOAuthProvider;
    
    @Autowired
    ClientRegistrationRepository clientRegistrationRepository;
    
    @Autowired
    private ClientRegistrationService clientRegistrationService;
    
    @Autowired
    private OauthProviderMapper oauthProviderMapper;
    
    @Autowired
    OauthProviderRestService oauthProviderRestService;
    

    //@BeforeEach
    void setUp() {
        oAuthProvider = new OAuthProvider(DISPLAY_NAME);
        azureOAuthProvider = new OAuthProvider(DISPLAY_NAME);
    }

    //@Test
    void getOAuthProviders() {
        when(clientRegistrationService.getAllExternalProviderIds()).thenReturn(List.of(oAuthProvider, azureOAuthProvider));
        ResponseEntity<List<RestOAuthProvider>> responseEntity = oauthProviderRestService.getOAuthProviders();
        assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
        assertThat(responseEntity.getBody(), is(notNullValue()));
        assertThat(responseEntity.getBody(), is(hasSize(2)));
    }
}