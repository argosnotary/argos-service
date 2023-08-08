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
package com.argosnotary.argos.service.rest.link;


import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.argosnotary.argos.domain.link.LinkMetaBlock;
import com.argosnotary.argos.domain.roles.Permission;
import com.argosnotary.argos.service.auditlog.AuditLog;
import com.argosnotary.argos.service.link.LinkMetaBlockService;
import com.argosnotary.argos.service.nodes.SupplyChainService;
import com.argosnotary.argos.service.openapi.rest.model.RestLinkMetaBlock;
import com.argosnotary.argos.service.roles.PermissionCheck;
import com.argosnotary.argos.service.verification.SignatureValidatorService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api")
public class LinkMetaBlockRestServiceImpl implements LinkMetaBlockRestService {

    private final LinkMetaBlockService linkMetaBlockService;

    private final SupplyChainService supplyChainService;

    private final LinkMetaBlockMapper linkMetaBlockMapper;

    private final SignatureValidatorService signatureValidatorService;

    @Override
    @PermissionCheck(permissions = Permission.LINK_ADD)
    @AuditLog
    @Transactional
    public ResponseEntity<RestLinkMetaBlock> createLink(UUID supplyChainId, RestLinkMetaBlock restLinkMetaBlock) {
        log.info("createLink supplyChainId : {}", supplyChainId);
        if (!supplyChainService.exists(supplyChainId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "supply chain not found : " + supplyChainId);
        }

        LinkMetaBlock linkMetaBlock = linkMetaBlockMapper.convertFromRestLinkMetaBlock(restLinkMetaBlock);
        if (!signatureValidatorService.validateSignature(linkMetaBlock.getLink(), linkMetaBlock.getSignature())) {
        	throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid signature");
        }
        linkMetaBlock.setSupplyChainId(supplyChainId);
        LinkMetaBlock link = linkMetaBlockService.create(linkMetaBlock);
        return ResponseEntity.status(HttpStatus.CREATED).body(linkMetaBlockMapper.convertToRestLinkMetaBlock(link));
    }

    @Override
    @PermissionCheck(permissions = Permission.READ)
    public ResponseEntity<List<RestLinkMetaBlock>> findLink(UUID supplyChainId, String hash) {
        if (!supplyChainService.exists(supplyChainId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "supply chain not found : " + supplyChainId);
        }

        return new ResponseEntity<>(linkMetaBlockService.find(supplyChainId, Optional.ofNullable(hash))
                .stream().map(linkMetaBlockMapper::convertToRestLinkMetaBlock).toList(), HttpStatus.OK);
    }

}
