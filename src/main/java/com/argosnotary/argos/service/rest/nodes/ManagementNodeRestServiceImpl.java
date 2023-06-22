package com.argosnotary.argos.service.rest.nodes;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.argosnotary.argos.domain.nodes.ManagementNode;
import com.argosnotary.argos.domain.nodes.Node;
import com.argosnotary.argos.domain.nodes.Organization;
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
		Optional<Node> parent = nodeService.findById(parentId);
		if (!(parent.isPresent() 
				&& parent.get().getId().equals(restManagementNode.getParentId()) 
				&& (parent.get() instanceof Organization || (parent.get() instanceof ManagementNode)))) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid parent"); 
		}
		
		ManagementNode node = managementNodeService
				.create(managementNodeMapper.convertFromRestManagementNode(restManagementNode));

        URI location = ServletUriComponentsBuilder
        		.fromPath("/managementnodes")
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
		managementNodeService.findById(managementNodeId).orElseThrow(this::managementNodeNotFound);
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
	public ResponseEntity<List<RestManagementNode>> getManagementNodes() {
		return ResponseEntity.ok(managementNodeService.find(Set.of())
				.stream()
				.map(managementNodeMapper::convertToRestManagementNode)
				.collect(Collectors.toList()));
	}
	
	private ResponseStatusException managementNodeNotFound() {
		return new ResponseStatusException(HttpStatus.NOT_FOUND, "ManagementNode not found");
	}
}
