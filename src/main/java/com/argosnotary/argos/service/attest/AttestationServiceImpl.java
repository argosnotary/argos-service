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
package com.argosnotary.argos.service.attest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.argosnotary.argos.domain.attest.Attestation;
import com.argosnotary.argos.service.mongodb.attest.AttestationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AttestationServiceImpl implements AttestationService {
	
	private final AttestationRepository attestationRepository;

	@Override
	public Attestation create(Attestation attestation) {
		return attestationRepository.insert(attestation);
	}

	@Override
	public void deleteBySupplyChainId(UUID supplyChainId) {
		attestationRepository.deleteBySupplyChainId(supplyChainId);
	}

	@Override
	public List<Attestation> find(UUID supplyChainId, Optional<String> optionalHash) {
		return optionalHash
				.map(hash -> attestationRepository.findBySupplyChainIdAndHash(supplyChainId, hash))
                .orElseGet(() -> attestationRepository.findBySupplyChainId(supplyChainId));
	}

}
