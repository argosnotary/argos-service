package com.argosnotary.argos.service.rest.roles;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import com.argosnotary.argos.service.openapi.rest.api.RoleAssignmentApi;
import com.argosnotary.argos.service.openapi.rest.model.RestRoleAssignment;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import jakarta.validation.Valid;

public interface RoleAssignmentRestService extends RoleAssignmentApi {

	@Override
	ResponseEntity<RestRoleAssignment> createRoleAssignment(
			@Parameter(name = "resourceId", description = "resource id on which assignment is created", required = true, in = ParameterIn.PATH) @PathVariable("resourceId") UUID resourceId,
			@Parameter(name = "RestRoleAssignment", description = "") @Valid @RequestBody(required = false) RestRoleAssignment restRoleAssignment);

	@Override
	ResponseEntity<Void> deleteRoleAssignemntById(
	        @Parameter(name = "resourceId", description = "resource id", required = true, in = ParameterIn.PATH) @PathVariable("resourceId") UUID resourceId,
	        @Parameter(name = "roleAssignmentId", description = "role assignment id", required = true, in = ParameterIn.PATH) @PathVariable("roleAssignmentId") UUID roleAssignmentId
	    );

	@Override
	ResponseEntity<List<RestRoleAssignment>> getRoleAssignments(
			@Parameter(name = "resourceId", description = "resource id on which assignment is created", required = true, in = ParameterIn.PATH) @PathVariable("resourceId") UUID resourceId);

}
