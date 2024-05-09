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
package com.argosnotary.argos.domain.attest;

import org.springframework.data.mongodb.core.index.Indexed;

import com.argosnotary.argos.domain.crypto.HashAlgorithm;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ArgosDigest implements Comparable<ArgosDigest>{
	@Indexed
	private final String hash;
	private final HashAlgorithm algorithm;
	
	@Override
	public int compareTo(ArgosDigest o) {
		return hash.compareTo(o.getHash());
	}

}
