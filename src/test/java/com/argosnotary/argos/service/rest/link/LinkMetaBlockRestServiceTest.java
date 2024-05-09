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
package com.argosnotary.argos.service.rest.link;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.sameInstance;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import com.argosnotary.argos.domain.crypto.Signature;
import com.argosnotary.argos.domain.link.Link;
import com.argosnotary.argos.domain.link.LinkMetaBlock;
import com.argosnotary.argos.domain.nodes.SupplyChain;
import com.argosnotary.argos.service.link.LinkMetaBlockService;
import com.argosnotary.argos.service.nodes.SupplyChainService;
import com.argosnotary.argos.service.openapi.rest.model.RestLinkMetaBlock;
import com.argosnotary.argos.service.verification.SignatureValidatorService;


@ExtendWith(MockitoExtension.class)
class LinkMetaBlockRestServiceTest {

    private static final UUID SUPPLY_CHAIN_ID = UUID.randomUUID();
    private static final String HASH = "hash";

    @Mock
    private LinkMetaBlockService linkMetaBlockService;

    @Mock
    private SupplyChainService supplyChainService;

    @Mock
    private LinkMetaBlockMapper converter;

    @Mock
    private SignatureValidatorService signatureValidatorService;

    @Mock
    private RestLinkMetaBlock restLinkMetaBlock;

    @Mock
    private LinkMetaBlock linkMetaBlock;

    private LinkMetaBlockRestService restService;

    @Mock
    private SupplyChain supplyChain;

    @Mock
    private Signature signature;

    @Mock
    private Link link;

    @BeforeEach
    void setUp() {
        restService = new LinkMetaBlockRestServiceImpl(linkMetaBlockService, supplyChainService, converter, signatureValidatorService);

    }

    @Test
    void createLinkValidSignature() {

        when(linkMetaBlock.getLink()).thenReturn(link);
        when(linkMetaBlock.getSignature()).thenReturn(signature);
        when(converter.convertFromRestLinkMetaBlock(restLinkMetaBlock)).thenReturn(linkMetaBlock);
        when(supplyChainService.exists(SUPPLY_CHAIN_ID)).thenReturn(true);
        when(signatureValidatorService.validateSignature(link, signature)).thenReturn(true);
        assertThat(restService.createLink(SUPPLY_CHAIN_ID, restLinkMetaBlock).getStatusCodeValue(), is(201));
        verify(linkMetaBlock).setSupplyChainId(SUPPLY_CHAIN_ID);
        verify(linkMetaBlockService).create(linkMetaBlock);
        verify(signatureValidatorService).validateSignature(link, signature);
    }

    @Test
    void findLink() {
        when(supplyChainService.exists(SUPPLY_CHAIN_ID)).thenReturn(true);
        when(linkMetaBlockService.find(SUPPLY_CHAIN_ID, Optional.of(HASH))).thenReturn(Collections.singletonList(linkMetaBlock));
        when(converter.convertToRestLinkMetaBlock(linkMetaBlock)).thenReturn(restLinkMetaBlock);
        ResponseEntity<List<RestLinkMetaBlock>> response = restService.findLink(SUPPLY_CHAIN_ID, HASH);
        assertThat(response.getBody(), hasSize(1));
        assertThat(response.getBody().get(0), sameInstance(restLinkMetaBlock));
        assertThat(response.getStatusCodeValue(), is(200));
    }

    @Test
    void findLinkUnknownSupplyChain() {
        when(supplyChainService.exists(SUPPLY_CHAIN_ID)).thenReturn(false);
        
        ResponseStatusException error = assertThrows(ResponseStatusException.class, () -> restService.findLink(SUPPLY_CHAIN_ID, HASH));
        assertThat(error.getStatusCode().value(), is(404));
    }
}
