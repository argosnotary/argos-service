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

import com.argosnotary.argos.domain.nodes.ManagementNode;
import com.argosnotary.argos.domain.nodes.Node;
import com.argosnotary.argos.domain.roles.Permission;
import com.argosnotary.argos.service.auditlog.AuditLog;
import com.argosnotary.argos.service.nodes.ManagementNodeService;
import com.argosnotary.argos.service.nodes.NodeService;
import com.argosnotary.argos.service.openapi.rest.model.RestManagementNode;
import com.argosnotary.argos.service.roles.PermissionCheck;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ManagementNodeRestServiceImpl implements ManagementNodeRestService {
	
	private final ManagementNodeService managementNodeService;
	
	private final NodeService nodeService;
	
	private final ManagementNodeMapper managementNodeMapper;

	@Override
    @PermissionCheck(permissions = Permission.WRITE)
    @AuditLog
	public ResponseEntity<RestManagementNode> createManagementNode(UUID parentId,
			@Valid RestManagementNode restManagementNode) {
		ManagementNode managementNode = managementNodeMapper.convertFromRestManagementNode(restManagementNode);
		Optional<Node> parent = nodeService.findById(parentId);
		if (!(parentId.equals(restManagementNode.getParentId())
				&& parent.isPresent() 
				&& parent.get().getId().equals(restManagementNode.getParentId()) 
				&& managementNode.isValidParentType(parent.get()))) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid parent"); 
		}
		
		if (nodeService.existsByParentIdAndName(restManagementNode.getParentId(), restManagementNode.getName())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
					String.format("Management Node with name [%s] already exists on parent [%s]", restManagementNode.getName(), restManagementNode.getParentId()));
		}
		
		ManagementNode node = managementNodeService
				.create(managementNode);

        URI location = UriComponentsBuilder
        		.fromPath("/api/managementnodes")
                .path("/{managementNodeId}")
                .buildAndExpand(node.getId())
                .toUri();
		return ResponseEntity.created(location).body(managementNodeMapper.convertToRestManagementNode(node));
	}

	@Override
    @PermissionCheck(permissions = Permission.WRITE)
    @AuditLog
    @Transactional
	public ResponseEntity<Void> deleteManagementNodeById(UUID managementNodeId) {
		if (!managementNodeService.exists(managementNodeId)) {
			throw managementNodeNotFound();
		}
		managementNodeService.delete(managementNodeId);
		return ResponseEntity.noContent().build();
	}

	@Override
    @PermissionCheck(permissions = Permission.READ)
	public ResponseEntity<RestManagementNode> getManagementNode(UUID managementNodeId) {
		ManagementNode node = managementNodeService.findById(managementNodeId).orElseThrow(this::managementNodeNotFound);
		return ResponseEntity.ok(managementNodeMapper.convertToRestManagementNode(node));
	}

	@Override
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<List<RestManagementNode>> getManagementNodes(UUID ancestorId) {
		Optional<Node> optNode = Optional.empty();
		if (ancestorId != null) {
			optNode = nodeService.findById(ancestorId);
			if (optNode.isEmpty()) {
				throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Node with id [%s] not found", ancestorId));
			}
		}
		return ResponseEntity.ok(managementNodeService.find(optNode)
				.stream()
				.map(managementNodeMapper::convertToRestManagementNode)
				.toList());
	}
	
	private ResponseStatusException managementNodeNotFound() {
		return new ResponseStatusException(HttpStatus.NOT_FOUND, "ManagementNode not found");
	}
}
