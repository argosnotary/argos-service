package com.argosnotary.argos.service.rest.roles;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

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
		URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{roleAssignmentId}")
                .buildAndExpand(ra.getId())
                .toUri();
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
				.collect(Collectors.toList()));
	}

}
