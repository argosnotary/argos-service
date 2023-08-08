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
package com.argosnotary.argos.service.rest.nodes;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import com.argosnotary.argos.domain.nodes.Node;
import com.argosnotary.argos.domain.nodes.Project;
import com.argosnotary.argos.domain.roles.Permission;
import com.argosnotary.argos.service.auditlog.AuditLog;
import com.argosnotary.argos.service.nodes.NodeService;
import com.argosnotary.argos.service.nodes.ProjectService;
import com.argosnotary.argos.service.openapi.rest.model.RestProject;
import com.argosnotary.argos.service.roles.PermissionCheck;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ProjectRestServiceImpl implements ProjectRestService {
	
	private final ProjectService projectService;
	
	private final NodeService nodeService;
	
	private final ProjectMapper projectMapper;

	@Override
    @PermissionCheck(permissions = Permission.WRITE)
    @AuditLog
	public ResponseEntity<RestProject> createProject(UUID parentId, @Valid RestProject restProject) {

		Optional<Node> parent = nodeService.findById(parentId);
		if (!(parentId.equals(restProject.getParentId())
				&& parent.isPresent() 
				&& parent.get().getId().equals(restProject.getParentId())
				&& Project.isValidParentType(parent.get()))) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid parent");
		}
		
		if (nodeService.existsByParentIdAndName(restProject.getParentId(), restProject.getName())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
					String.format("Project with name [%s] already exists on parent [%s]", restProject.getName(), restProject.getParentId()));
		}
		
		Project node = projectService
				.create(projectMapper.convertFromRestProject(restProject));

        URI location = UriComponentsBuilder
        		.fromPath("/api/projects")
                .path("/{projectId}")
                .buildAndExpand(node.getId())
                .toUri();
		return ResponseEntity.created(location).body(projectMapper.convertToRestProject(node));
	}

	@Override
    @PermissionCheck(permissions = Permission.WRITE)
    @Transactional
    @AuditLog
	public ResponseEntity<Void> deleteProjectById(UUID projectId) {
		if (!projectService.exists(projectId)) {
			throw projectNotFound();
		}
		projectService.delete(projectId);
		return ResponseEntity.noContent().build();
	}

	@Override
    @PermissionCheck(permissions = Permission.READ)
	public ResponseEntity<RestProject> getProject(UUID projectId) {
		Project node = projectService.findById(projectId).orElseThrow(this::projectNotFound);
		return ResponseEntity.ok(projectMapper.convertToRestProject(node));
	}

	@Override
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<List<RestProject>> getProjects(UUID ancestorId) {
		Optional<Node> optNode = Optional.empty();
		if (ancestorId != null) {
			optNode = nodeService.findById(ancestorId);
			if (optNode.isEmpty()) {
				throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Node with id [%s] not found", ancestorId));
			}
		}
		return ResponseEntity.ok(projectService.find(optNode)
				.stream().map(projectMapper::convertToRestProject).toList());
	}
	
	private ResponseStatusException projectNotFound() {
		return new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found");
	}
}
