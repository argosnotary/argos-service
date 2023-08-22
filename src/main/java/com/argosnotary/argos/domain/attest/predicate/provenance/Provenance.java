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
package com.argosnotary.argos.domain.attest.predicate.provenance;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import com.argosnotary.argos.domain.ArgosError;
import com.argosnotary.argos.domain.attest.Predicate;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Provenance extends Predicate {
	@JsonIgnore
	public static final String PREDICATE_TYPE = "https://slsa.dev/provenance/v1";
	
	private final BuildDefinition buildDefinition;
	
	private final RunDetails runDetails;

	public Provenance(BuildDefinition buildDefinition, RunDetails runDetails) {
		super();
		this.buildDefinition = buildDefinition;
		this.runDetails = runDetails;
	}

	@Override
	@JsonIgnore
	public URL getPredicateType() {
		try {
			return new URL(PREDICATE_TYPE);
		} catch (MalformedURLException e) {
			throw new ArgosError(e.getMessage());
		}
	}
}