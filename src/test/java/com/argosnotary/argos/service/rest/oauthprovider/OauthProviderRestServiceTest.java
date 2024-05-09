/*
 * Argos Notary - A new way to secure the Software Supply Chain
 *
 * Copyright (C) 2019 - 2020 Rabobank Nederland
 * Copyright (C) 2019 - 2024 Gerard Borst <gerard.borst@argosnotary.com>
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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.argosnotary.argos.service.account.ClientRegistrationService;
import com.argosnotary.argos.service.account.OAuthProvider;
import com.argosnotary.argos.service.openapi.rest.model.RestOAuthProvider;

@ExtendWith(MockitoExtension.class)
class OauthProviderRestServiceTest {
    protected static final String DISPLAY_NAME = "displayName";
    protected static final String AZURE = "azure";
    protected static final String ICON_URL = "iconUrl";
    private OAuthProvider oAuthProvider;
    private OAuthProvider azureOAuthProvider;
    
    @Mock
    ClientRegistrationService clientRegistrationService;
    
    private OauthProviderRestService oauthProviderRestService;
    
    private OauthProviderMapper oauthProviderMapper = Mappers.getMapper(OauthProviderMapper.class);

    @BeforeEach
    void setUp() {
    	oauthProviderRestService = new OauthProviderRestServiceImpl(clientRegistrationService, oauthProviderMapper);
        oAuthProvider = new OAuthProvider(DISPLAY_NAME);
        azureOAuthProvider = new OAuthProvider(DISPLAY_NAME);        
    }

    @Test
    void getOAuthProviders() {
        when(clientRegistrationService.getAllExternalProviderIds()).thenReturn(List.of(oAuthProvider, azureOAuthProvider));
        ResponseEntity<List<RestOAuthProvider>> responseEntity = oauthProviderRestService.getOAuthProviders();
        assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
        assertThat(responseEntity.getBody(), is(List.of(oauthProviderMapper.convertToRestOauthProvider(azureOAuthProvider), oauthProviderMapper.convertToRestOauthProvider(oAuthProvider))));
        assertThat(responseEntity.getBody(), is(hasSize(2)));
    }
    
    @Test
    void getOAuthProvidersEmpty() {
        when(clientRegistrationService.getAllExternalProviderIds()).thenReturn(List.of());
        ResponseEntity<List<RestOAuthProvider>> responseEntity = oauthProviderRestService.getOAuthProviders();
        assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
        assertThat(responseEntity.getBody(), is(List.of()));
        assertThat(responseEntity.getBody(), is(hasSize(0)));
    }
}