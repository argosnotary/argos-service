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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.argosnotary.argos.domain.attest.ResourceDescriptor;
import com.argosnotary.argos.domain.crypto.signing.Canonicalable;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
@lombok.Builder
public class Builder implements Canonicalable<Builder>{
	private final String id;
    private final List<ResourceDescriptor>  builderDependencies;
    private final Map<String, String> version;

	public Builder(String id, List<ResourceDescriptor> builderDependencies, Map<String, String> version) {
		super();
		this.id = id;
		this.builderDependencies =  builderDependencies == null ? null : Collections.unmodifiableList(builderDependencies);
		this.version =  version == null ? null : Collections.unmodifiableMap(version);
	}
    
	@Override
	public Builder cloneCanonical() {
		return new Builder(
				id, 
				builderDependencies == null ? null : builderDependencies.stream().sorted().map(ResourceDescriptor::cloneCanonical).toList(), 
						version == null ? null : new TreeMap<>(version));
	}
	
}
