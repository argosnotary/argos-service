/*
 * Argos Notary - A new way to secure the Software Supply Chain
 *
 * Copyright (C) 2019 - 2020 Rabobank Nederland
 * Copyright (C) 2019 - 2025 Gerard Borst <gerard.borst@argosnotary.com>
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
package com.argosnotary.argos.service.rest.verification;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.argosnotary.argos.service.openapi.rest.api.VerificationApi;
import com.argosnotary.argos.service.openapi.rest.model.RestArtifact;
import com.argosnotary.argos.service.openapi.rest.model.RestVerificationResult;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public interface VerificationRestService extends VerificationApi {

	@Override
	public ResponseEntity<RestVerificationResult> getVerification(
			@NotNull @Size(max = 4096) @Parameter(name = "artifactHashes", description = "", required = true, in = ParameterIn.QUERY) @Valid @RequestParam(value = "artifactHashes", required = true) List<String> artifactHashes,
			@Size(max = 20) @Parameter(name = "paths", description = "", in = ParameterIn.QUERY) @Valid @RequestParam(value = "paths", required = false) List<String> paths);

	@Override
	public ResponseEntity<RestVerificationResult> performVerification(
			@Parameter(name = "supplyChainId", description = "supply chain id", required = true, in = ParameterIn.PATH) @PathVariable("supplyChainId") UUID supplyChainId,
			@Parameter(name = "RestArtifact", description = "", required = true) @Valid @Size(max = 12288) @RequestBody List<RestArtifact> expectedProducts);

}
