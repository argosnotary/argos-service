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
package com.argosnotary.argos.service.rest.verification;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.argosnotary.argos.domain.layout.LayoutMetaBlock;
import com.argosnotary.argos.domain.link.Artifact;
import com.argosnotary.argos.domain.roles.Permission;
import com.argosnotary.argos.service.auditlog.AuditLog;
import com.argosnotary.argos.service.layout.LayoutMetaBlockService;
import com.argosnotary.argos.service.nodes.SupplyChainService;
import com.argosnotary.argos.service.openapi.rest.model.RestArtifact;
import com.argosnotary.argos.service.openapi.rest.model.RestVerificationResult;
import com.argosnotary.argos.service.rest.ArtifactMapper;
import com.argosnotary.argos.service.roles.PermissionCheck;
import com.argosnotary.argos.service.verification.VerificationRunResult;
import com.argosnotary.argos.service.verification.VerificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api")
public class VerificationRestServiceImpl implements VerificationRestService {

    private final VerificationService verificationService;

    private final SupplyChainService supplyChainService;

    private final LayoutMetaBlockService layoutMetaBlockService;
    
    private final ArtifactMapper artifactMapper;

    private final VerificationResultMapper verificationResultMapper;

    @Override
    public ResponseEntity<RestVerificationResult> getVerification(List<String> artifactHashes, List<String> paths) {
        log.info("Verification request for paths [{}] and hashes [{}].", paths, artifactHashes);
        boolean isvalid = verificationService.getVerification(artifactHashes, paths);
        log.info("Verify result [{}] for paths [{}] and hashes [{}].", isvalid, paths, artifactHashes);
        return ResponseEntity.ok(new RestVerificationResult(isvalid));
    }

    @Override
    @PermissionCheck(permissions = Permission.READ)
    @AuditLog
    public ResponseEntity<RestVerificationResult> performVerification(UUID supplyChainId, List<RestArtifact> expectedProducts) {
    	if (!supplyChainService.exists(supplyChainId)) {
    		return ResponseEntity.ok(
    				verificationResultMapper.mapToRestVerificationResult(VerificationRunResult.builder().runIsValid(false).build()));
    	}

        LayoutMetaBlock layoutMetaBlock = layoutMetaBlockService.getLayout(supplyChainId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "no active layout could be found for supplychain:" + supplyChainId));

        Set<Artifact> products = expectedProducts.stream().map(artifactMapper::restArtifactToArtifact).collect(Collectors.toSet());
        return ResponseEntity.ok(
        		verificationResultMapper.mapToRestVerificationResult(verificationService.performVerification(layoutMetaBlock, products)));
    }
}
