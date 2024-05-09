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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.argosnotary.argos.domain.attest.Attestation;
import com.argosnotary.argos.domain.attest.AttestationData;
import com.argosnotary.argos.domain.crypto.Signature;
import com.argosnotary.argos.service.mongodb.attest.AttestationRepository;

@ExtendWith(MockitoExtension.class)
class AttestationServiceTest {
	
	private static final Map<String, Attestation> DATA_MAP = AttestationData.createTestData();

    private static final UUID SUPPLY_CHAIN_ID = DATA_MAP.get("at1").getSupplyChainId();
	
	@Mock
	private AttestationRepository attestationRepository;
	
    private static final String HASH_1 = "hash1";
    
    private Attestation at1, at2;
    
    AttestationService attestationService;

	@BeforeEach
	void setUp() throws Exception {
		attestationService = new AttestationServiceImpl(attestationRepository);
    	at1 = DATA_MAP.get("at1");
    	at2 = DATA_MAP.get("at2");
	}

	@Test
	void testSave() {
		when(attestationRepository.insert(at1)).thenReturn(at1);
		Attestation a = attestationService.create(at1);
		assertEquals(at1, a);
	}
	
	@Test
	void testDeleteBySupplyChainId() {
		attestationService.deleteBySupplyChainId(SUPPLY_CHAIN_ID);
		verify(attestationRepository).deleteBySupplyChainId(SUPPLY_CHAIN_ID);
	}
	
	@Test
	void testFindBySupplyChainId() {
		when(attestationRepository.findBySupplyChainId(SUPPLY_CHAIN_ID)).thenReturn(List.of(at1, at2));
		List<Attestation> l = attestationService.find(SUPPLY_CHAIN_ID, Optional.empty());
		verify(attestationRepository, never()).findBySupplyChainIdAndHash(any(), any());
		assertEquals(List.of(at1, at2), l);
	}
	
	@Test
	void testFindBySupplyChainIdNoReturn() {
		when(attestationRepository.findBySupplyChainId(SUPPLY_CHAIN_ID)).thenReturn(List.of());
		List<Attestation> l = attestationService.find(SUPPLY_CHAIN_ID, Optional.empty());
		verify(attestationRepository, never()).findBySupplyChainIdAndHash(any(), any());
		assertEquals(List.of(), l);
	}
    
	@Test
	void testFindBySupplyChainAndSha() {
		when(attestationRepository.findBySupplyChainIdAndHash(SUPPLY_CHAIN_ID, HASH_1)).thenReturn(List.of(at1, at2));
		List<Attestation> l = attestationService.find(SUPPLY_CHAIN_ID, Optional.of(HASH_1));
		verify(attestationRepository, never()).findBySupplyChainId(any());
		assertEquals(List.of(at1, at2), l);
	}
    
	@Test
	void testFindBySupplyChainAndShaEmptyReturn() {
		when(attestationRepository.findBySupplyChainIdAndHash(SUPPLY_CHAIN_ID, HASH_1)).thenReturn(List.of());
		List<Attestation> l = attestationService.find(SUPPLY_CHAIN_ID, Optional.of(HASH_1));
		verify(attestationRepository, never()).findBySupplyChainId(any());
		assertEquals(List.of(), l);
	}
    
    private Signature createSignature() {
        return Signature.builder()
                .keyId("2392017103413adf6fa3b535e3714b30bc0a901229d0e76784f5ffca653f905e")
                .sig("signature")
                .build();
    }

}
