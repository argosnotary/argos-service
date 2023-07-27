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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import com.argosnotary.argos.domain.nodes.Node;
import com.argosnotary.argos.domain.nodes.Project;
import com.argosnotary.argos.domain.nodes.SupplyChain;
import com.argosnotary.argos.domain.roles.Permission;
import com.argosnotary.argos.service.auditlog.AuditLog;
import com.argosnotary.argos.service.nodes.NodeService;
import com.argosnotary.argos.service.nodes.SupplyChainService;
import com.argosnotary.argos.service.openapi.rest.model.RestSupplyChain;
import com.argosnotary.argos.service.roles.PermissionCheck;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class SupplyChainRestServiceImpl implements SupplyChainRestService {
	
	private final SupplyChainService supplyChainService;
	
	private final NodeService nodeService;
	
	private final SupplyChainMapper supplyChainMapper;

	@Override
    @PermissionCheck(permissions = Permission.WRITE)
    @AuditLog
	public ResponseEntity<RestSupplyChain> createSupplyChain(UUID projectId, @Valid RestSupplyChain restSupplyChain) {

		Optional<Node> parent = nodeService.findById(projectId);
		if (!(parent.isPresent() 
				&& parent.get().getId().equals(restSupplyChain.getParentId()) 
				&& (parent.get() instanceof Project))) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid parent"); 
		}
		
		if (nodeService.existsByParentIdAndName(restSupplyChain.getParentId(), restSupplyChain.getName())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
					String.format("Supply Chain with name [%s] already exists on project [%s]", restSupplyChain.getName(), restSupplyChain.getParentId()));
		}
		
		SupplyChain node = supplyChainService
				.create(supplyChainMapper.convertFromRestSupplyChain(restSupplyChain));

        URI location = UriComponentsBuilder
        		.fromPath("/supplychains")
                .path("/{supplyChainId}")
                .buildAndExpand(node.getId())
                .toUri();
		return ResponseEntity.created(location).body(supplyChainMapper.convertToRestSupplyChain(node));
	}

	@Override
    @PermissionCheck(permissions = Permission.WRITE)
    @Transactional
    @AuditLog
	public ResponseEntity<Void> deleteSupplyChainById(UUID supplyChainId) {
		if (!supplyChainService.exists(supplyChainId)) {
			throw supplyChainNotFound();
		}
		supplyChainService.delete(supplyChainId);
		return ResponseEntity.noContent().build();
	}

	@Override
    @PermissionCheck(permissions = Permission.READ)
	public ResponseEntity<RestSupplyChain> getSupplyChain(UUID supplyChainId) {
		SupplyChain node = supplyChainService.findById(supplyChainId).orElseThrow(this::supplyChainNotFound);
		return ResponseEntity.ok(supplyChainMapper.convertToRestSupplyChain(node));
	}

	@Override
    @PermissionCheck(permissions = Permission.READ)
	public ResponseEntity<List<RestSupplyChain>> getSupplyChains(UUID ancestorId) {
		Optional<Node> optNode = nodeService.findById(ancestorId);
		if (optNode.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Node with id [%s] not found", ancestorId));
		}
		return ResponseEntity.ok(supplyChainService.find(optNode.get())
				.stream().map(supplyChainMapper::convertToRestSupplyChain).toList());
	}

	@Override
    @PermissionCheck(permissions = Permission.WRITE)
    @AuditLog
	public ResponseEntity<RestSupplyChain> updateSupplyChain(UUID supplyChainId,
			@Valid RestSupplyChain restSupplyChain) {
		if (!nodeService.exists(Project.class, restSupplyChain.getParentId())) {
			throw parentProjectNotFound(); 
		}
		Optional<SupplyChain> supplyChain = supplyChainService.findById(supplyChainId);
		if (!supplyChain.isPresent()) {
			throw supplyChainNotFound();
		}
		if (!(restSupplyChain.getId() != null
				&& restSupplyChain.getId().equals(supplyChain.get().getId()) 
				&& (supplyChain.get() instanceof SupplyChain))) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid supply chain");
		}
		
		SupplyChain node = supplyChainService
				.update(supplyChainMapper.convertFromRestSupplyChain(restSupplyChain));

        URI location = UriComponentsBuilder
        		.fromPath("/supplychains")
                .path("/{supplyChainId}")
                .buildAndExpand(node.getId())
                .toUri();
		return ResponseEntity.created(location).body(supplyChainMapper.convertToRestSupplyChain(node));
	}
	
	private ResponseStatusException parentProjectNotFound() {
		return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Parent project not found");
	}
	
	private ResponseStatusException supplyChainNotFound() {
		return new ResponseStatusException(HttpStatus.NOT_FOUND, "SupplyChain not found");
	}

}
