package com.argosnotary.argos.service.rest.nodes;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import com.argosnotary.argos.service.openapi.rest.api.ManagementNodeApi;
import com.argosnotary.argos.service.openapi.rest.model.RestManagementNode;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import jakarta.validation.Valid;

public interface ManagementNodeRestService extends ManagementNodeApi {

	@Override
	ResponseEntity<RestManagementNode> createManagementNode(
			@Parameter(name = "parentId", description = "this will be the parent id", required = true, in = ParameterIn.PATH) @PathVariable("parentId") UUID parentId,
			@Parameter(name = "RestManagementNode", description = "") @Valid @RequestBody(required = false) RestManagementNode restManagementNode);

	@Override
	ResponseEntity<Void> deleteManagementNodeById(
			@Parameter(name = "managementNodeId", description = "management node id", required = true, in = ParameterIn.PATH) @PathVariable("managementNodeId") UUID managementNodeId);

	@Override
	ResponseEntity<RestManagementNode> getManagementNode(
			@Parameter(name = "managementNodeId", description = "this will be the management node id", required = true, in = ParameterIn.PATH) @PathVariable("managementNodeId") UUID managementNodeId);

	@Override
	ResponseEntity<List<RestManagementNode>> getManagementNodes(
			@Parameter(name = "parentId", description = "this is be the parent id", required = true, in = ParameterIn.PATH) @PathVariable("parentId") UUID parentId);
}
