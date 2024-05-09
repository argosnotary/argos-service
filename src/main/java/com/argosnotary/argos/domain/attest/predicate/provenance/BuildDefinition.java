/*
 * Argos Notary - A new way to secure the Software Supply Chain
 *
 * Copyright (C) 2019 - 2020 Rabobank Nederland
 * Copyright (C) 2019 - 2024 Gerard Borst <gerard.borst@argosnotary.com>
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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.argosnotary.argos.domain.attest.ResourceDescriptor;
import com.argosnotary.argos.domain.crypto.signing.Canonicalable;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
@EqualsAndHashCode
public class BuildDefinition implements Canonicalable<BuildDefinition>{
	
	private final String buildType;
	private final Map<String, String> externalParameters;
	private final Map<String, String> internalParameters;
    private final List<ResourceDescriptor> resolvedDependencies;

	public BuildDefinition(String buildType, Map<String, String> externalParameters,
	        Map<String, String> internalParameters, List<ResourceDescriptor> resolvedDependencies) {
		super();
		this.buildType = buildType;
		this.externalParameters = externalParameters == null ? null : Collections.unmodifiableMap(externalParameters);
		this.internalParameters = internalParameters == null ? null : Collections.unmodifiableMap(internalParameters);
		this.resolvedDependencies = resolvedDependencies == null ? null : Collections.unmodifiableList(resolvedDependencies);
	}
    
	@Override
	public BuildDefinition cloneCanonical() {
		return new BuildDefinition(buildType, externalParameters == null ? null : new TreeMap<>(externalParameters), 
				internalParameters == null ? null : new TreeMap<>(internalParameters), 
				resolvedDependencies == null ? null : resolvedDependencies.stream().sorted().map(ResourceDescriptor::cloneCanonical).toList());
	}

}
