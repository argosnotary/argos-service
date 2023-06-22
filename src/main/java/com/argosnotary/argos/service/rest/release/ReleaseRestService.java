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
package com.argosnotary.argos.service.rest.release;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.argosnotary.argos.domain.link.Artifact;
import com.argosnotary.argos.domain.release.ReleaseResult;
import com.argosnotary.argos.domain.roles.Permission;
import com.argosnotary.argos.service.auditlog.AuditLog;
import com.argosnotary.argos.service.openapi.rest.api.ReleaseApi;
import com.argosnotary.argos.service.openapi.rest.model.RestReleaseArtifacts;
import com.argosnotary.argos.service.openapi.rest.model.RestReleaseResult;
import com.argosnotary.argos.service.release.ReleaseService;
import com.argosnotary.argos.service.roles.PermissionCheck;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ReleaseRestService implements ReleaseApi {

    private final ReleaseService releaseService;
    private final ReleaseArtifactMapper artifactMapper;
    private final ReleaseResultMapper releaseResultMapper;

    @Override
    @PermissionCheck(permissions = Permission.RELEASE)
    @AuditLog
    public ResponseEntity<RestReleaseResult> createRelease(UUID supplyChainId, RestReleaseArtifacts restReleaseArtifacts) {
        List<Set<Artifact>> artifacts = artifactMapper.mapToArtifacts(restReleaseArtifacts.getReleaseArtifacts());
        ReleaseResult releaseResult = releaseService.createRelease(supplyChainId, artifacts);
        return ResponseEntity.ok(releaseResultMapper.maptoRestReleaseResult(releaseResult));
    }
}
