package com.argosnotary.argos.service.rest.nodes;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import com.argosnotary.argos.service.openapi.rest.api.ProjectApi;
import com.argosnotary.argos.service.openapi.rest.model.RestProject;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import jakarta.validation.Valid;

public interface ProjectRestService extends ProjectApi {

	@Override
	ResponseEntity<RestProject> createProject(
			@Parameter(name = "parentId", description = "this will be the parent id", required = true, in = ParameterIn.PATH) @PathVariable("parentId") UUID parentId,
			@Parameter(name = "RestProject", description = "") @Valid @RequestBody(required = false) RestProject restProject);

	@Override
	ResponseEntity<Void> deleteProjectById(
			@Parameter(name = "projectId", description = "project id", required = true, in = ParameterIn.PATH) @PathVariable("projectId") UUID projectId);

	@Override
	ResponseEntity<RestProject> getProject(
			@Parameter(name = "projectId", description = "this will be the project id", required = true, in = ParameterIn.PATH) @PathVariable("projectId") UUID projectId);

	@Override
	ResponseEntity<List<RestProject>> getProjects(
			@Parameter(name = "parentId", description = "this will be the parent id", required = true, in = ParameterIn.PATH) @PathVariable("parentId") UUID parentId);

}
