package com.argosnotary.argos.service.rest.account;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import com.argosnotary.argos.service.openapi.rest.api.ServiceAccountApi;
import com.argosnotary.argos.service.openapi.rest.model.RestKeyPair;
import com.argosnotary.argos.service.openapi.rest.model.RestServiceAccount;
import com.argosnotary.argos.service.openapi.rest.model.RestServiceAccountKeyPair;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import jakarta.validation.Valid;

public interface ServiceAccountRestService extends ServiceAccountApi {

	@Override
	ResponseEntity<RestServiceAccount> createServiceAccount(
			@Parameter(name = "projectId", description = "", required = true, in = ParameterIn.PATH) @PathVariable("projectId") UUID projectId,
			@Parameter(name = "RestServiceAccount", description = "") @Valid @RequestBody(required = false) RestServiceAccount restServiceAccount);

	@Override
	ResponseEntity<RestKeyPair> createServiceAccountKeyById(
			@Parameter(name = "projectId", description = "", required = true, in = ParameterIn.PATH) @PathVariable("projectId") UUID projectId,
			@Parameter(name = "serviceAccountId", description = "", required = true, in = ParameterIn.PATH) @PathVariable("serviceAccountId") UUID serviceAccountId,
			@Parameter(name = "RestServiceAccountKeyPair", description = "") @Valid @RequestBody(required = false) RestServiceAccountKeyPair restServiceAccountKeyPair);

	@Override
	ResponseEntity<Void> deleteServiceAccount(
			@Parameter(name = "projectId", description = "", required = true, in = ParameterIn.PATH) @PathVariable("projectId") UUID projectId,
			@Parameter(name = "serviceAccountId", description = "", required = true, in = ParameterIn.PATH) @PathVariable("serviceAccountId") UUID serviceAccountId);

	@Override
	ResponseEntity<RestServiceAccount> getServiceAccountById(
			@Parameter(name = "projectId", description = "", required = true, in = ParameterIn.PATH) @PathVariable("projectId") UUID projectId,
			@Parameter(name = "serviceAccountId", description = "", required = true, in = ParameterIn.PATH) @PathVariable("serviceAccountId") UUID serviceAccountId);

	@Override
	ResponseEntity<RestKeyPair> getServiceAccountKey(

	);

	@Override
	ResponseEntity<RestKeyPair> getServiceAccountKeyById(
			@Parameter(name = "projectId", description = "", required = true, in = ParameterIn.PATH) @PathVariable("projectId") UUID projectId,
			@Parameter(name = "serviceAccountId", description = "", required = true, in = ParameterIn.PATH) @PathVariable("serviceAccountId") UUID serviceAccountId);
}
