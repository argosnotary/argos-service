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
package com.argosnotary.argos.domain.attest;

import java.net.URI;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.data.mongodb.core.index.Indexed;

import com.argosnotary.argos.domain.ArgosError;
import com.argosnotary.argos.domain.crypto.signing.Canonicalable;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ResourceDescriptor implements Comparable<ResourceDescriptor>,Canonicalable<ResourceDescriptor>{
	private final Map<String,String> digest;
	@NotNull
	private final URI uri;
	
	@NotNull
	@Indexed
	private final ArgosDigest argosDigest;

	public ResourceDescriptor(Map<String, String> digest, @NotNull URI uri, @NotNull ArgosDigest argosDigest) {
		if (uri == null || argosDigest == null) {
			throw new ArgosError(String.format("Wrong ResourceDescriptor defintion, properties are null: uri: [%s], argosDigest: [%s]", uri == null ? null : uri.toString(), argosDigest == null ? null : argosDigest.toString()));
		}
		this.digest = digest;
		this.uri = uri;
		this.argosDigest = argosDigest;
	}

	@Override
	public int compareTo(ResourceDescriptor o) {
		int result = uri.toString().compareTo(o.getUri().toString());
		if (result == 0) {
			return argosDigest.compareTo(o.getArgosDigest());
		}
		return result;
	}

	@Override
	public ResourceDescriptor cloneCanonical() {
		return new ResourceDescriptor(digest == null ? null : new TreeMap<>(digest), uri, new ArgosDigest(argosDigest.getHash(), argosDigest.getAlgorithm() == null ? null : argosDigest.getAlgorithm()));
	}

}
