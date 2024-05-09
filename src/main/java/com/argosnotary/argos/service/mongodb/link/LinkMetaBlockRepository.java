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
package com.argosnotary.argos.service.mongodb.link;

import java.util.List;
import java.util.UUID;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.argosnotary.argos.domain.link.LinkMetaBlock;

public interface LinkMetaBlockRepository extends MongoRepository<LinkMetaBlock, UUID> {
	
	public void deleteBySupplyChainId(UUID supplyChainId);
	
	public List<LinkMetaBlock> findBySupplyChainId(UUID supplyChainId);
	
	@Query("{$and: [{supplyChainId: ?0}, {$or: [{'link.materials.hash': ?1}, {'link.products.hash': ?1}]}]}")
	public List<LinkMetaBlock> findBySupplyChainIdAndHash(UUID supplyChainId, String hash);
}
