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
		
		SupplyChain node = supplyChainService
				.create(supplyChainMapper.convertFromRestSupplyChain(restSupplyChain));

        URI location = ServletUriComponentsBuilder
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
    @PreAuthorize("isAuthenticated()")
	public ResponseEntity<List<RestSupplyChain>> getSupplyChains() {
		return ResponseEntity.ok(supplyChainService.find(Set.of())
				.stream().map(supplyChainMapper::convertToRestSupplyChain).collect(Collectors.toList()));
	}

	@Override
    @PermissionCheck(permissions = Permission.READ)
	public ResponseEntity<List<RestSupplyChain>> getSupplyChainsForProject(UUID projectId) {
		return ResponseEntity.ok(supplyChainService.find(Set.of(projectId))
				.stream().map(supplyChainMapper::convertToRestSupplyChain).collect(Collectors.toList()));
	}

	@Override
    @PermissionCheck(permissions = Permission.WRITE)
    @AuditLog
	public ResponseEntity<RestSupplyChain> updateSupplyChain(UUID supplyChainId,
			@Valid RestSupplyChain restSupplyChain) {
		if (!nodeService.exists(Project.class.getCanonicalName(), restSupplyChain.getParentId())) {
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

        URI location = ServletUriComponentsBuilder
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
