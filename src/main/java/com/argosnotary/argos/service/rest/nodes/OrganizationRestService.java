package com.argosnotary.argos.service.rest.nodes;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import com.argosnotary.argos.service.openapi.rest.api.OrganizationApi;
import com.argosnotary.argos.service.openapi.rest.model.RestOrganization;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import jakarta.validation.Valid;

public interface OrganizationRestService extends OrganizationApi {

	@Override
	public ResponseEntity<RestOrganization> createOrganization(
			@Parameter(name = "RestOrganization", description = "") @Valid @RequestBody(required = false) RestOrganization restOrganization);

	@Override
	public ResponseEntity<Void> deleteOrganizationById(
			@Parameter(name = "organizationId", description = "organization id", required = true, in = ParameterIn.PATH) @PathVariable("organizationId") UUID organizationId);

	@Override
	public ResponseEntity<RestOrganization> getOrganization(
			@Parameter(name = "organizationId", description = "this will be the organizationId id", required = true, in = ParameterIn.PATH) @PathVariable("organizationId") UUID organizationId);

	@Override
	public ResponseEntity<List<RestOrganization>> getOrganizations(

	);

}
