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
package com.argosnotary.argos.domain.release;

import static java.lang.String.join;
import static org.apache.commons.codec.digest.DigestUtils.sha256Hex;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.argosnotary.argos.domain.nodes.Domain;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
@Document(collection="releases")
@CompoundIndex(def = "{'organization.domain.domain' : 1}")
public class Release {
	@Id
	private UUID id;
	private String name;
    private OffsetDateTime releaseDate;
    private String qualifiedSupplyChainName;
    private Domain domain;
    @Indexed
    private UUID supplyChainId;
    @Indexed
    private Set<String> releasedProductsHashes;
    private ObjectId dossierId;

    @Indexed
    private String releasedProductsHashesHash;

    public static String calculateReleasedProductsHashesHash(Set<String> artifactList) {
        List<String> list = new ArrayList<>(artifactList);
        Collections.sort(list);
        return sha256Hex(join("", list));
    }

}
