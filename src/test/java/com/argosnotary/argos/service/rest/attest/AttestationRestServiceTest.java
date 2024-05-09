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
package com.argosnotary.argos.service.rest.attest;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import com.argosnotary.argos.domain.attest.Attestation;
import com.argosnotary.argos.domain.attest.AttestationData;
import com.argosnotary.argos.service.attest.AttestationService;
import com.argosnotary.argos.service.nodes.SupplyChainService;
import com.argosnotary.argos.service.openapi.rest.model.RestAttestation;
import com.argosnotary.argos.service.verification.SignatureValidatorService;

import jakarta.servlet.http.HttpServletRequest;

@ExtendWith(MockitoExtension.class)
class AttestationRestServiceTest {
	
	private static final Map<String, Attestation> DATA_MAP = AttestationData.createTestData();

    private static final UUID SUPPLY_CHAIN_ID = DATA_MAP.get("at1").getSupplyChainId();
	
	private static final UUID OTHER_SUPPLY_CHAIN_ID = UUID.randomUUID();
    private static final String HASH_1 = "hash1";
    private static final String AT1 = "at1";
    private static final String AT2 = "at2";
    
    private Attestation at1, at2;
    
    private RestAttestation rat2;
    
    Map<String, Attestation> dataMap = AttestationData.createTestData();

	AttestationRestService attestationRestService;
	
	private AttestationMapper attestationMapper = Mappers.getMapper(AttestationMapper.class);

    @Mock
    private HttpServletRequest httpServletRequest;
	
	@Mock
	private AttestationService attestationService;
	
	@Mock
	private SupplyChainService supplyChainService;
	
	@Mock
	private SignatureValidatorService signatureValidatorService;

	@BeforeEach
	void setUp() throws Exception {
		attestationRestService = new AttestationRestServiceImpl(attestationMapper, attestationService, supplyChainService, signatureValidatorService);
    	at1 = dataMap.get(AT1);
    	at2 = dataMap.get(AT2);
    	rat2 = attestationMapper.convertToRestAttestation(dataMap.get(AT2));
    	
	}

	@Test
	void testCreateAttestation() {
		when(attestationService.create(at2)).thenReturn(at2);
		when(supplyChainService.exists(SUPPLY_CHAIN_ID)).thenReturn(true);
		when(signatureValidatorService.validateSignature(at2.getEnvelope().getPayload(), at2.getEnvelope().getSignatures().get(1))).thenReturn(true);
		when(signatureValidatorService.validateSignature(at2.getEnvelope().getPayload(), at2.getEnvelope().getSignatures().get(0))).thenReturn(true);
		ResponseEntity<RestAttestation> resp = attestationRestService.createAttestation(SUPPLY_CHAIN_ID, rat2);
		assertThat(resp.getStatusCode().value(), is(201));
		assertEquals(rat2, resp.getBody());
		
	}

	@Test
	void testCreateAttestationSigNotValid() {
		when(supplyChainService.exists(SUPPLY_CHAIN_ID)).thenReturn(true);
		when(signatureValidatorService.validateSignature(at2.getEnvelope().getPayload(), at2.getEnvelope().getSignatures().get(0))).thenReturn(false);
		
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
        	attestationRestService.createAttestation(SUPPLY_CHAIN_ID, rat2);
          });
        
        assertEquals(String.format("400 BAD_REQUEST \"invalid signature with key id [%s]\"", at2.getEnvelope().getSignatures().get(0).getKeyId()), exception.getMessage());
        assertThat(exception.getStatusCode().value(), is(400));
		
	}

	@Test
	void testCreateAttestationInvalidSUPPLY_CHAIN_ID() {
		ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
        	attestationRestService.createAttestation(OTHER_SUPPLY_CHAIN_ID, rat2);
          });
        
        assertEquals("400 BAD_REQUEST \"invalid supply chain\"", exception.getMessage());
        assertThat(exception.getStatusCode().value(), is(400));
		
	}

	@Test
	void testCreateAttestationSupplyChainNotExists() {

		when(supplyChainService.exists(SUPPLY_CHAIN_ID)).thenReturn(false);
		ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
        	attestationRestService.createAttestation(SUPPLY_CHAIN_ID, rat2);
          });
        
        assertEquals("400 BAD_REQUEST \"invalid supply chain\"", exception.getMessage());
        assertThat(exception.getStatusCode().value(), is(400));
		
	}
	
	@Test
	void testGetAttestations() {
		when(supplyChainService.exists(SUPPLY_CHAIN_ID)).thenReturn(true);
		when(attestationService.find(SUPPLY_CHAIN_ID, Optional.of(HASH_1))).thenReturn(List.of(at1, at2));
		ResponseEntity<List<RestAttestation>> resp = attestationRestService.getAttestations(SUPPLY_CHAIN_ID, HASH_1);
		List<Attestation> la = resp.getBody().stream().map(a -> attestationMapper.convertFromRestAttestation(a)).toList();
		assertEquals(List.of(at1, at2), la);
        assertThat(resp.getStatusCode().value(), is(200));
		
	}

	@Test
	void testGetAttestationsSupplyChainNotExists() {

		when(supplyChainService.exists(SUPPLY_CHAIN_ID)).thenReturn(false);
		ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
			attestationRestService.getAttestations(SUPPLY_CHAIN_ID, HASH_1);
          });
        
        assertEquals(String.format("404 NOT_FOUND \"supply chain not found : %s\"",SUPPLY_CHAIN_ID), exception.getMessage());
        assertThat(exception.getStatusCode().value(), is(404));
		
	}

}
