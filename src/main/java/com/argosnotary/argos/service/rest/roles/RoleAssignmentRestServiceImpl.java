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
package com.argosnotary.argos.service.rest.roles;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.argosnotary.argos.domain.roles.Permission;
import com.argosnotary.argos.domain.roles.RoleAssignment;
import com.argosnotary.argos.service.openapi.rest.model.RestRoleAssignment;
import com.argosnotary.argos.service.roles.PermissionCheck;
import com.argosnotary.argos.service.roles.RoleAssignmentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class RoleAssignmentRestServiceImpl implements RoleAssignmentRestService {
	
	private final RoleAssignmentService roleAssignmentService;
	
	private final RoleAssignmentMapper roleAssignmentMapper;

	@Override
    @PermissionCheck(permissions = Permission.ROLE_WRITE)
	public ResponseEntity<RestRoleAssignment> createRoleAssignment(UUID resourceId,
			@Valid RestRoleAssignment restRoleAssignment) {
		RoleAssignment ra = roleAssignmentService.save(roleAssignmentMapper.convertFromRestRoleAssignment(restRoleAssignment));
		URI location = UriComponentsBuilder
        		.fromPath("/api/roleassignments/{roleassignmentId}")
				.buildAndExpand(ra.getId()).toUri();
		return ResponseEntity.created(location).body(roleAssignmentMapper.convertToRestRoleAssignment(ra));
	}

	@Override
    @PermissionCheck(permissions = Permission.ROLE_WRITE)
	public ResponseEntity<Void> deleteRoleAssignemntById(UUID resourceId, UUID roleAssignmentId) {
		roleAssignmentService.delete(roleAssignmentId);
		return ResponseEntity.noContent().build();
	}

	@Override
    @PermissionCheck(permissions = Permission.ROLE_WRITE)
	public ResponseEntity<List<RestRoleAssignment>> getRoleAssignments(UUID resourceId) {
		return ResponseEntity.ok(roleAssignmentService.findByResourceId(resourceId).stream()
				.map(roleAssignmentMapper::convertToRestRoleAssignment)
				.toList());
	}

}
