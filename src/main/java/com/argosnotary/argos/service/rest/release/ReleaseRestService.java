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
package com.argosnotary.argos.service.rest.release;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import com.argosnotary.argos.service.openapi.rest.api.ReleaseApi;
import com.argosnotary.argos.service.openapi.rest.model.RestReleaseArtifacts;
import com.argosnotary.argos.service.openapi.rest.model.RestReleaseResult;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import jakarta.validation.Valid;

public interface ReleaseRestService extends ReleaseApi {

	@Override
	public ResponseEntity<RestReleaseResult> createRelease(
			@Parameter(name = "supplyChainId", description = "supply chain id", required = true, in = ParameterIn.PATH) @PathVariable("supplyChainId") UUID supplyChainId,
			@Parameter(name = "RestReleaseArtifacts", description = "", required = true) @Valid @RequestBody RestReleaseArtifacts restReleaseArtifacts);

}
