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

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.argosnotary.argos.service.account.ClientRegistrationService;
import com.argosnotary.argos.service.openapi.rest.model.RestOAuthProvider;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class OauthProviderRestServiceImpl implements OauthProviderRestService {
    private final ClientRegistrationService clientRegistrationService;
    
    private final OauthProviderMapper oauthProviderMapper;

    @Override
    public ResponseEntity<List<RestOAuthProvider>> getOAuthProviders() {
        List<RestOAuthProvider> restOAuthProviders = oauthProviderMapper
        		.convertToRestOAuthProviderList(clientRegistrationService.getAllExternalProviderIds());
        
        return ResponseEntity.ok(restOAuthProviders);
    }
}

