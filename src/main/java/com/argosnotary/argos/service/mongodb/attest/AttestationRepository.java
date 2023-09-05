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
package com.argosnotary.argos.service.mongodb.attest;

import java.util.List;
import java.util.UUID;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.argosnotary.argos.domain.attest.Attestation;

@Repository
public interface AttestationRepository  extends MongoRepository<Attestation, UUID> {
	
	public void deleteBySupplyChainId(UUID supplyChainId);
	
	@Query("{supplyChainId: ?0}")
	public List<Attestation> findBySupplyChainId(UUID argosSupplyChainId);
	
	@Query("{$and: [{supplyChainId: ?0}, {'envelope.payload.subject.argosDigest.hash': ?1}]}")
	public List<Attestation> findBySupplyChainIdAndHash(UUID supplyChainId, String hash);

}
