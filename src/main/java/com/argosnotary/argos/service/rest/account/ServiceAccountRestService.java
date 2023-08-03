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
package com.argosnotary.argos.service.rest.account;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import com.argosnotary.argos.service.openapi.rest.api.ServiceAccountApi;
import com.argosnotary.argos.service.openapi.rest.model.RestJwtToken;
import com.argosnotary.argos.service.openapi.rest.model.RestKeyPair;
import com.argosnotary.argos.service.openapi.rest.model.RestServiceAccount;
import com.argosnotary.argos.service.openapi.rest.model.RestServiceAccountKeyPair;
import com.argosnotary.argos.service.openapi.rest.model.RestTokenRequest;

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
	ResponseEntity<RestJwtToken> getIdToken(
			@Parameter(name = "RestTokenRequest", description = "") @Valid @RequestBody(required = false) RestTokenRequest restTokenRequest);

	@Override
	ResponseEntity<RestKeyPair> getServiceAccountKeyById(
			@Parameter(name = "projectId", description = "", required = true, in = ParameterIn.PATH) @PathVariable("projectId") UUID projectId,
			@Parameter(name = "serviceAccountId", description = "", required = true, in = ParameterIn.PATH) @PathVariable("serviceAccountId") UUID serviceAccountId);
}
