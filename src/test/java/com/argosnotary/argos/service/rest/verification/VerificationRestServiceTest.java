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
package com.argosnotary.argos.service.rest.verification;

import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import com.argosnotary.argos.domain.layout.LayoutMetaBlock;
import com.argosnotary.argos.domain.link.Artifact;
import com.argosnotary.argos.service.layout.LayoutMetaBlockService;
import com.argosnotary.argos.service.nodes.SupplyChainService;
import com.argosnotary.argos.service.openapi.rest.model.RestArtifact;
import com.argosnotary.argos.service.openapi.rest.model.RestVerificationResult;
import com.argosnotary.argos.service.rest.ArtifactMapper;
import com.argosnotary.argos.service.verification.VerificationRunResult;
import com.argosnotary.argos.service.verification.VerificationService;

@ExtendWith(MockitoExtension.class)
class VerificationRestServiceTest {
	private static final UUID SUPPLYCHAIN_ID = UUID.randomUUID(); 

    @Mock
    private VerificationService verificationService;

    @Mock
    private SupplyChainService supplyChainService;

    @Mock
    private LayoutMetaBlockService layoutMetaBlockService;

    private ArtifactMapper artifactMapper;

    private VerificationResultMapper verificationResultMapper;

    private Artifact artifact;

    private RestArtifact restArtifact;

    @Mock
    private LayoutMetaBlock layoutMetaBlockMetaBlock;

    private VerificationRestService verificationRestService;


    @BeforeEach
    void setup() {
        artifactMapper = Mappers.getMapper(ArtifactMapper.class);
        verificationResultMapper = Mappers.getMapper(VerificationResultMapper.class);
        
        verificationRestService = new VerificationRestServiceImpl(
        		verificationService,
        		supplyChainService,
                layoutMetaBlockService,
                artifactMapper,
                verificationResultMapper);
        artifact = Artifact.builder().hash("hash").uri("uri").build();
        restArtifact = new RestArtifact("hash","uri");

    }
    
    @Test
    void testArtifactMapper() {
    	List<Artifact> ff = List.of(artifactMapper.restArtifactToArtifact(restArtifact));
        assertThat(ff.size(), is(1));
    }

    @Test
    void performVerificationShouldReturnOk() {
        VerificationRunResult runResult = VerificationRunResult.okay();
        RestVerificationResult restVerificationResult = new RestVerificationResult();
        restVerificationResult.setRunIsValid(true);
        when(supplyChainService.exists(SUPPLYCHAIN_ID)).thenReturn(true);
        when(layoutMetaBlockService.getLayout(SUPPLYCHAIN_ID))
                .thenReturn(Optional.of(layoutMetaBlockMetaBlock));
        when(verificationService.performVerification(layoutMetaBlockMetaBlock, Set.of(artifact))).thenReturn(runResult);
        ResponseEntity<RestVerificationResult> result = verificationRestService.performVerification(SUPPLYCHAIN_ID, List.of(restArtifact));
        assertThat(result.getStatusCode(), is(HttpStatus.OK));
        assertThat(result.getBody().getRunIsValid(), is(true));
    }

    @Test
    void performVerificationWithNoLayoutShouldReturnError() {
        when(supplyChainService.exists(SUPPLYCHAIN_ID)).thenReturn(true);
        when(layoutMetaBlockService.getLayout(SUPPLYCHAIN_ID))
                .thenReturn(Optional.empty());
        List<RestArtifact> l =singletonList(restArtifact);
        ResponseStatusException error = assertThrows(ResponseStatusException.class, () -> verificationRestService.performVerification(SUPPLYCHAIN_ID, l));
        assertThat(error.getStatusCode().value(), is(400));
    }


    @Test
    void getVerification() {
        when(verificationService.getVerification(List.of("hash"), List.of("path"))).thenReturn(true);
        ResponseEntity<RestVerificationResult> result = verificationRestService.getVerification(List.of("hash"), List.of("path"));
        assertThat(result.getStatusCode(), is(HttpStatus.OK));
        assertThat(result.getBody().getRunIsValid(), is(true));
    }
}