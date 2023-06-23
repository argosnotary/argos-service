/*
 * Argos Notary - A new way to secure the Software Supply Chain
 *
 * Copyright (C) 2019 - 2020 Rabobank Nederland
 * Copyright (C) 2019 - 2022 Gerard Borst <gerard.borst@argosnotary.com>
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.web.servlet.MockMvc;

import com.argosnotary.argos.domain.link.Artifact;
import com.argosnotary.argos.domain.nodes.Organization;
import com.argosnotary.argos.domain.release.Release;
import com.argosnotary.argos.domain.release.ReleaseResult;
import com.argosnotary.argos.service.account.AccountService;
import com.argosnotary.argos.service.nodes.NodeService;
import com.argosnotary.argos.service.openapi.rest.model.RestArtifact;
import com.argosnotary.argos.service.openapi.rest.model.RestReleaseArtifacts;
import com.argosnotary.argos.service.openapi.rest.model.RestReleaseResult;
import com.argosnotary.argos.service.release.ReleaseService;
import com.argosnotary.argos.service.security.helpers.LogContextHelper;
import com.fasterxml.jackson.databind.ObjectMapper;


//@SpringBootTest
//@AutoConfigureMockMvc
@WebMvcTest(controllers=ReleaseRestServiceImpl.class) //, excludeAutoConfiguration = SecurityConfig.class)
@AutoConfigureMockMvc(addFilters = false)
class ReleaseRestServiceTest {


    private static final String APPLICATION_JSON = "application/json";
    @MockBean
    private ReleaseService releaseService;
    @MockBean
    private ReleaseArtifactMapper artifactMapper; // = Mappers.getMapper(ReleaseArtifactMapper.class);
    @MockBean
    private ReleaseResultMapper releaseResultMapper; // = Mappers.getMapper(ReleaseResultMapper.class);
    @MockBean
    private LogContextHelper logContextHelper;
    
    @MockBean
    private NodeService nodeService;
    
    @MockBean
    private AccountService accountService;

    @Mock
    private RestReleaseArtifacts restReleaseArtifacts;

    @Mock
    private RestArtifact restArtifact;

    private ReleaseResult releaseResult;

    @Mock
    private RestReleaseResult restReleaseResult;

    private Artifact artifact;

    private Organization org;
    
    @Mock
    private ObjectMapper mapper;

    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private ClientRegistrationRepository mockClientRegistrationRepository;
    
    @Autowired
    ApplicationContext context;
    
    Set<String> releaseArtifactHashes;
    
    UUID supplyChainId;


    @BeforeEach
    void setUp() {
    	supplyChainId = UUID.fromString("1b721ae0-3320-442e-af78-6f57cf35f4e6");
    	artifact = new Artifact("49e73a11c5e689db448d866ce08848ac5886cac8aa31156ea4de37427aca6162", "uri");
    	
    	releaseArtifactHashes = Set.of("49e73a11c5e689db448d866ce08848ac5886cac8aa31156ea4de37427aca6162");
        Release release = Release.builder()
        		.id(UUID.randomUUID())
        				.releasedProductsHashes(releaseArtifactHashes)
                        .releaseDate(OffsetDateTime.now(ZoneOffset.UTC))
        				.organization(org)
        				.supplyChainId(supplyChainId)
        				.build();
        releaseResult = ReleaseResult
                .builder()
                .releaseIsValid(true)
                .release(release)
                .build();
    }

    @Test
    void createReleaseShouldReturn200() throws Exception {
    	when(artifactMapper.mapToArtifacts(any())).thenReturn(List.of(Set.of(artifact)));
    	when(releaseService.createRelease(supplyChainId, List.of(Set.of(artifact)))).thenReturn(releaseResult);
        String releaseRequestBody = "{\n"
        		+ "    \"releaseArtifacts\": [\n"
        		+ "        [\n"
        		+ "            {\n"
        		+ "                \"uri\": \"target/argos-test-0.0.1-SNAPSHOT.jar\",\n"
        		+ "                \"hash\": \"49e73a11c5e689db448d866ce08848ac5886cac8aa31156ea4de37427aca6162\"\n"
        		+ "            }\n"
        		+ "        ]\n"
        		+ "    ]\n"
        		+ "}";
        mockMvc.perform(post("/api/supplychains/1b721ae0-3320-442e-af78-6f57cf35f4e6/release")
                .contentType(APPLICATION_JSON)
                .content(releaseRequestBody))
        .andExpect(status().isCreated());
    }
}