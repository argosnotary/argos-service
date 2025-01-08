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
package com.argosnotary.argos.service.rest.attest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.argosnotary.argos.domain.attest.Attestation;
import com.argosnotary.argos.domain.roles.Permission;
import com.argosnotary.argos.service.attest.AttestationService;
import com.argosnotary.argos.service.auditlog.AuditLog;
import com.argosnotary.argos.service.nodes.SupplyChainService;
import com.argosnotary.argos.service.openapi.rest.model.RestAttestation;
import com.argosnotary.argos.service.roles.PermissionCheck;
import com.argosnotary.argos.service.verification.SignatureValidatorService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class AttestationRestServiceImpl implements AttestationRestService {
	
	private final AttestationMapper attestationMapper;
	
	private final AttestationService attestationService;
	
	private final SupplyChainService supplyChainService;
	
	private final SignatureValidatorService signatureValidatorService;

	@Override
    @PermissionCheck(permissions = Permission.ATTESTATION_ADD)
    @AuditLog
	public ResponseEntity<RestAttestation> createAttestation(UUID supplyChainId, RestAttestation restAttestation) {
		Attestation attestation = attestationMapper.convertFromRestAttestation(restAttestation);
		if (!(supplyChainId.equals(attestation.getSupplyChainId())
				&& supplyChainService.exists(supplyChainId))) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid supply chain"); 
		}
		
		// all signatures should be valid
		attestation.getEnvelope().getSignatures().stream().filter(s -> !signatureValidatorService
				.validateSignature(attestation.getEnvelope().getPayload(), s)).findAny().ifPresent(s -> {
					throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("invalid signature with key id [%s]", s.getKeyId()));
					});

		Attestation attest = attestationService.create(attestation);
        return ResponseEntity.status(HttpStatus.CREATED).body(attestationMapper.convertToRestAttestation(attest));
	}

	@Override
    @PermissionCheck(permissions = Permission.READ)
	public ResponseEntity<List<RestAttestation>> getAttestations(UUID supplyChainId, String hash) {
        if (!supplyChainService.exists(supplyChainId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "supply chain not found : " + supplyChainId);
        }

        return new ResponseEntity<>(attestationService.find(supplyChainId, Optional.ofNullable(hash))
                .stream().map(attestationMapper::convertToRestAttestation).toList(), HttpStatus.OK);
	}

}
