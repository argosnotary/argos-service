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
package com.argosnotary.argos.service.rest.release;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.argosnotary.argos.domain.link.Artifact;
import com.argosnotary.argos.domain.nodes.Domain;
import com.argosnotary.argos.domain.release.Release;
import com.argosnotary.argos.domain.release.ReleaseResult;
import com.argosnotary.argos.service.openapi.rest.model.RestArtifact;
import com.argosnotary.argos.service.openapi.rest.model.RestReleaseArtifacts;
import com.argosnotary.argos.service.openapi.rest.model.RestReleaseResult;
import com.argosnotary.argos.service.release.ReleaseService;
import com.argosnotary.argos.service.rest.ArtifactMapper;
import com.argosnotary.argos.service.rest.ArtifactMapperImpl;
import com.fasterxml.jackson.databind.json.JsonMapper;

import jakarta.servlet.http.HttpServletRequest;


@SpringBootTest(classes= {ArtifactMapperImpl.class, ReleaseResultMapperImpl.class, JsonMapper.class})
class ReleaseRestServiceTest {


    private static final String APPLICATION_JSON = "application/json";
    
    @Mock
    private ReleaseService releaseService;
    
    @Autowired
    private ArtifactMapper artifactMapper;
    
    @Autowired
    private ReleaseResultMapper releaseResultMapper;
    
    ReleaseRestService releaseRestService;

    private ReleaseResult releaseResult;

    @Mock
    private RestReleaseResult restReleaseResult;
	
	@Mock
    private HttpServletRequest httpServletRequest;

    private Artifact artifact;

    private RestArtifact ra;
    
    RestReleaseArtifacts res;

    private Domain domain;
    
    Set<String> releaseArtifactHashes;
    
    UUID supplyChainId;


    @BeforeEach
    void setUp() {
    	releaseRestService = new ReleaseRestServiceImpl(releaseService, artifactMapper, releaseResultMapper);
    	supplyChainId = UUID.fromString("1b721ae0-3320-442e-af78-6f57cf35f4e6");
    	artifact = new Artifact("49e73a11c5e689db448d866ce08848ac5886cac8aa31156ea4de37427aca6162", "uri");
    	
    	releaseArtifactHashes = Set.of("49e73a11c5e689db448d866ce08848ac5886cac8aa31156ea4de37427aca6162");
    	ra = new RestArtifact(artifact.getHash(), artifact.getUri());
    	res = new RestReleaseArtifacts().releaseArtifacts(List.of(List.of(ra)));
        Release release = Release.builder()
        		.id(UUID.randomUUID())
        				.releasedProductsHashes(releaseArtifactHashes)
                        .releaseDate(OffsetDateTime.now(ZoneOffset.UTC))
                        .domain(domain)
                        .dossierId(ObjectId.get())
        				.supplyChainId(supplyChainId)
        				.build();
        releaseResult = ReleaseResult
                .builder()
                .releaseIsValid(true)
                .release(release)
                .build();
        restReleaseResult = releaseResultMapper.maptoRestReleaseResult(releaseResult);
    }

    @Test
    void createReleaseShouldReturn200() throws Exception {
		ServletRequestAttributes servletRequestAttributes = new ServletRequestAttributes(httpServletRequest);
        RequestContextHolder.setRequestAttributes(servletRequestAttributes);
    	when(releaseService.createRelease(supplyChainId, List.of(Set.of(artifact)))).thenReturn(releaseResult);
    	ResponseEntity<RestReleaseResult> response = releaseRestService.createRelease(supplyChainId, res);
        assertThat(response.getStatusCode(), is(HttpStatusCode.valueOf(200)));
        assertEquals(restReleaseResult, response.getBody());
    }
}