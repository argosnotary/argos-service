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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.web.servlet.MockMvc;

import com.argosnotary.argos.domain.link.Artifact;
import com.argosnotary.argos.domain.release.ReleaseDossierMetaData;
import com.argosnotary.argos.domain.release.ReleaseResult;
import com.argosnotary.argos.service.openapi.rest.model.RestArtifact;
import com.argosnotary.argos.service.openapi.rest.model.RestReleaseArtifacts;
import com.argosnotary.argos.service.openapi.rest.model.RestReleaseResult;
import com.argosnotary.argos.service.release.ReleaseService;
import com.fasterxml.jackson.databind.ObjectMapper;


@WebMvcTest(ReleaseRestService.class)
@Profile("test")
class ReleaseRestServiceTest {


    private static final String APPLICATION_JSON = "application/json";
    @Mock
    private ReleaseService releaseService;
    @Mock
    private ReleaseArtifactMapper artifactMapper;
    @Mock
    private ReleaseResultMapper releaseResultMapper;

    @Mock
    private RestReleaseArtifacts restReleaseArtifacts;

    @Mock
    private RestArtifact restArtifact;

    private ReleaseResult releaseResult;

    @Mock
    private RestReleaseResult restReleaseResult;

    @Mock
    private Artifact artifact;
    
    @Mock
    private ObjectMapper mapper;


    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private ClientRegistrationRepository mockClientRegistrationRepository;
    
//    @Autowired
//    ApplicationContext context;


    //@BeforeEach
    void setUp() {
    	List<List<String>> releaseArtifactHashes = List.of(List.of("49e73a11c5e689db448d866ce08848ac5886cac8aa31156ea4de37427aca6162"));
        ReleaseDossierMetaData releaseDossierMetaData = ReleaseDossierMetaData.builder()
                .releaseArtifacts(releaseArtifactHashes)
                .releaseDate(OffsetDateTime.now(ZoneOffset.UTC))
                .supplyChainPath("supplyChainPath")
                .build();
        releaseResult = ReleaseResult
                .builder()
                .releaseIsValid(true)
                .releaseDossierMetaData(releaseDossierMetaData)
                .build();
    }

    //@Test
    void createReleaseShouldReturn200() throws Exception {
    	//given(mockClientRegistrationRepository..createCustomer(customer)).willReturn(persistedCustomer);
        when(releaseService.createRelease(any(), any())).thenReturn(releaseResult);
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
        mockMvc.perform(post("/api/supplychain/1b721ae0-3320-442e-af78-6f57cf35f4e6/release")
                .contentType(APPLICATION_JSON)
                .content(releaseRequestBody))
        .andExpect(status().isCreated())
        .andExpect(content().string("{\"message\":\"Duplicate\"}"));
    }
}