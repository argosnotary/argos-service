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
package com.argosnotary.argos.service.rest.layout;

import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;

import com.argosnotary.argos.domain.account.Account;
import com.argosnotary.argos.domain.crypto.KeyPair;
import com.argosnotary.argos.domain.layout.ApprovalConfiguration;
import com.argosnotary.argos.domain.layout.ArtifactCollectorSpecification;
import com.argosnotary.argos.domain.layout.Layout;
import com.argosnotary.argos.domain.layout.LayoutMetaBlock;
import com.argosnotary.argos.domain.layout.ReleaseConfiguration;
import com.argosnotary.argos.domain.layout.Step;
import com.argosnotary.argos.service.account.AccountSecurityContext;
import com.argosnotary.argos.service.layout.LayoutMetaBlockService;
import com.argosnotary.argos.service.openapi.rest.model.RestApprovalConfiguration;
import com.argosnotary.argos.service.openapi.rest.model.RestLayoutMetaBlock;
import com.argosnotary.argos.service.openapi.rest.model.RestReleaseConfiguration;

import jakarta.servlet.http.HttpServletRequest;

@SpringBootTest(classes={LayoutMetaBlockMapperImpl.class,StepMapperImpl.class,RuleMapperImpl.class,MatchRuleMapperImpl.class})
@ExtendWith(MockitoExtension.class)
class LayoutMetaBlockRestServiceTest {

    private static final String SEGMENT_NAME = "segmentName";
    private static final String STEP_NAME = "stepName";
    private static final UUID SUPPLY_CHAIN_ID = UUID.randomUUID();

    @Autowired
    private LayoutMetaBlockMapper converter;

    @Mock
    private LayoutMetaBlockService layoutMetaBlockService;

    private RestLayoutMetaBlock restLayoutMetaBlock;
    
    private ApprovalConfigurationMapper approvalConfigurationMapper = Mappers.getMapper(ApprovalConfigurationMapper.class);

    private ReleaseConfigurationMapper releaseConfigurationMapper = Mappers.getMapper(ReleaseConfigurationMapper.class);

    private Layout layout;

    private LayoutMetaBlock layoutMetaBlock;

    @Mock
    private HttpServletRequest httpServletRequest;

    private LayoutMetaBlockRestService service;

    @Mock
    private LayoutValidatorService validator;

    private RestApprovalConfiguration restApprovalConfiguration;

    private ApprovalConfiguration approvalConfiguration;
    
    @Mock
    private AccountSecurityContext accountSecurityContext;

    @Mock
    private Account account;

    @Mock
    private KeyPair keyPair;

    private ReleaseConfiguration releaseConfiguration;

    private RestReleaseConfiguration restReleaseConfiguration;

    @Mock
    private Step step;

    @BeforeEach
    void setUp() throws URISyntaxException {
        service = new LayoutMetaBlockRestServiceImpl(converter, layoutMetaBlockService, validator, approvalConfigurationMapper, releaseConfigurationMapper);
        ArtifactCollectorSpecification spec = ArtifactCollectorSpecification.builder().context(Map.of("applicationName", "bla")).uri(new URI("bla")).name("spec").type(ArtifactCollectorSpecification.CollectorType.XLDEPLOY).build();
        approvalConfiguration = ApprovalConfiguration.builder()
        		.artifactCollectorSpecifications(List.of(spec))
        		.supplyChainId(SUPPLY_CHAIN_ID)
        		.stepName(STEP_NAME).build();
        restApprovalConfiguration = approvalConfigurationMapper.convertToRestApprovalConfiguration(approvalConfiguration);
        releaseConfiguration = ReleaseConfiguration.builder().artifactCollectorSpecifications(List.of(spec)).supplyChainId(SUPPLY_CHAIN_ID).build();
        restReleaseConfiguration = releaseConfigurationMapper.convertToRestReleaseConfiguration(releaseConfiguration);
        layout = Layout.builder().authorizedKeyIds(List.of()).steps(createStep()).build();
        layoutMetaBlock = LayoutMetaBlock.builder().layout(layout).signatures(List.of()).supplyChainId(SUPPLY_CHAIN_ID).build();
        restLayoutMetaBlock = converter.convertToRestLayoutMetaBlock(layoutMetaBlock);
    }

    @Test
    void createOrUpdateLayout() {
        ServletRequestAttributes servletRequestAttributes = new ServletRequestAttributes(httpServletRequest);
        RequestContextHolder.setRequestAttributes(servletRequestAttributes);
        when(layoutMetaBlockService.save(layoutMetaBlock)).thenReturn(layoutMetaBlock);
        ResponseEntity<RestLayoutMetaBlock> responseEntity = service.createOrUpdateLayout(SUPPLY_CHAIN_ID, restLayoutMetaBlock);
        assertThat(responseEntity.getStatusCode().value(), is(201));
        assertThat(responseEntity.getBody(), is(restLayoutMetaBlock));
        assertThat(Objects.requireNonNull(responseEntity.getHeaders().getLocation()).getPath(), is(""));
        verify(layoutMetaBlockService).save(layoutMetaBlock);
        verify(validator).validate(layoutMetaBlock);

    }

    @Test
    void validateLayoutValid() {
        ResponseEntity responseEntity = service.validateLayout(SUPPLY_CHAIN_ID, restLayoutMetaBlock.getLayout());
        assertThat(responseEntity.getStatusCode().value(), is(204));
        verify(validator).validateLayout(layout);
    }

    @Test
    void getLayout() {
        when(layoutMetaBlockService.getLayout(SUPPLY_CHAIN_ID)).thenReturn(Optional.of(layoutMetaBlock));
        ResponseEntity<RestLayoutMetaBlock> responseEntity = service.getLayout(SUPPLY_CHAIN_ID);
        assertThat(responseEntity.getStatusCode().value(), is(200));
        assertThat(responseEntity.getBody(), is(restLayoutMetaBlock));
    }

    @Test
    void getLayoutNotFound() {
        when(layoutMetaBlockService.getLayout(SUPPLY_CHAIN_ID)).thenReturn(Optional.empty());
        ResponseStatusException responseStatusException = assertThrows(ResponseStatusException.class, () -> service.getLayout(SUPPLY_CHAIN_ID));
        assertThat(responseStatusException.getStatusCode(), is(HttpStatus.NOT_FOUND));
        assertThat(responseStatusException.getReason(), is("layout not found"));
    }

    @Test
    void createApprovalConfigurationShouldStoreLayout() {
    	when(layoutMetaBlockService.getLayout(SUPPLY_CHAIN_ID)).thenReturn(Optional.of(layoutMetaBlock));
    	when(layoutMetaBlockService.stepNameExistInLayout(layout, STEP_NAME)).thenReturn(true);
        when(layoutMetaBlockService.createApprovalConfigurations(List.of(approvalConfiguration))).thenReturn(List.of(approvalConfiguration));
        ResponseEntity<List<RestApprovalConfiguration>> responseEntity = service.
                createApprovalConfigurations(SUPPLY_CHAIN_ID, List.of(restApprovalConfiguration));
        assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
        verify(layoutMetaBlockService).createApprovalConfigurations(List.of(approvalConfiguration));
    }

    @Test
    void createApprovalConfigurationWithIncorrectStepNameShouldThrowValidationError() {

        Layout layoutWrong = Layout.builder().authorizedKeyIds(List.of()).steps(List.of(Step.builder().name("wrong-step").build())).build();
        
        when(layoutMetaBlockService.getLayout(SUPPLY_CHAIN_ID)).thenReturn(Optional.of(LayoutMetaBlock.builder().layout(layoutWrong).build()));
        List<RestApprovalConfiguration> configs = List.of(restApprovalConfiguration);
        LayoutValidationException layoutValidationException = assertThrows(LayoutValidationException.class, () ->
                service.createApprovalConfigurations(SUPPLY_CHAIN_ID, configs)
        );

        assertThat(layoutValidationException.getValidationMessages().isEmpty(), is(false));
        assertThat(layoutValidationException.getValidationMessages().get(0).getField(), is("stepName"));
        assertThat(layoutValidationException.getValidationMessages().get(0).getMessage(), is("step with name: stepName does not exist in layout"));
    }

    @Test
    void createApprovalConfigurationsWithoutExistingLayoutShouldThrowValidationError() {
        when(layoutMetaBlockService.getLayout(SUPPLY_CHAIN_ID)).thenReturn(Optional.empty());
        List<RestApprovalConfiguration> configs = List.of(restApprovalConfiguration);

        ResponseStatusException responseStatusException = assertThrows(ResponseStatusException.class, () ->
                service.createApprovalConfigurations(SUPPLY_CHAIN_ID, configs)
        );
        assertThat(responseStatusException.getStatusCode(), is(HttpStatus.NOT_FOUND));
        assertThat(responseStatusException.getMessage(), is("404 NOT_FOUND \"layout not found\""));
    }


    @Test
    void createApprovalConfigurationsWithIncorrectArtifactSpecificationShouldThrowValidationError() {
    	ArtifactCollectorSpecification col = ArtifactCollectorSpecification.builder().context(Map.of()).name("bla").type(ArtifactCollectorSpecification.CollectorType.XLDEPLOY).build();
    	ApprovalConfiguration conf = ApprovalConfiguration.builder().artifactCollectorSpecifications(List.of(col)).build();
        List<RestApprovalConfiguration> l = List.of(approvalConfigurationMapper.convertToRestApprovalConfiguration(conf));
        LayoutValidationException layoutValidationException = assertThrows(LayoutValidationException.class, () -> service.createApprovalConfigurations(SUPPLY_CHAIN_ID, l));
        assertThat(layoutValidationException.getValidationMessages().isEmpty(), is(false));
        assertThat(layoutValidationException.getValidationMessages().get(0).getField(), is("context"));
        assertThat(layoutValidationException.getValidationMessages().get(0).getMessage(), is("required fields : [applicationName] not present for collector type: XLDEPLOY"));
    }


    @Test
    void getApprovalConfigurations() {
        when(layoutMetaBlockService.getApprovalConfigurations(SUPPLY_CHAIN_ID)).thenReturn(List.of(approvalConfiguration));
        ResponseEntity<List<RestApprovalConfiguration>> responseEntity = service.getApprovalConfigurations(SUPPLY_CHAIN_ID);
        assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
        assertThat(responseEntity.getBody(), hasSize(1));
    }

    @Test
    void getApprovalsForAccount() {
        when(layoutMetaBlockService.getApprovalsForAccount(SUPPLY_CHAIN_ID)).thenReturn(List.of(approvalConfiguration));
        ResponseEntity<List<RestApprovalConfiguration>> responseEntity = service.getApprovalsForAccount(SUPPLY_CHAIN_ID);
        assertThat(responseEntity.getStatusCode().value(), is(200));
        assertThat(responseEntity.getBody(), contains(restApprovalConfiguration));
    }

    @Test
    void getApprovalsForAccountNoLayout() {
        when(layoutMetaBlockService.getApprovalsForAccount(SUPPLY_CHAIN_ID)).thenReturn(List.of());
        ResponseEntity<List<RestApprovalConfiguration>> responseEntity = service.getApprovalsForAccount(SUPPLY_CHAIN_ID);
        assertThat(responseEntity.getStatusCode().value(), is(200));
        assertThat(responseEntity.getBody(), empty());

    }

    @Test
    void getReleaseConfiguration() {
        when(layoutMetaBlockService.getReleaseConfiguration(SUPPLY_CHAIN_ID)).thenReturn(Optional.of(releaseConfiguration));
        ResponseEntity<RestReleaseConfiguration> response = service.getReleaseConfiguration(SUPPLY_CHAIN_ID);
        assertThat(response.getBody(), is(restReleaseConfiguration));
        assertThat(response.getStatusCode().value(), is(200));
    }

    @Test
    void getReleaseConfigurationNotFound() {
        when(layoutMetaBlockService.getReleaseConfiguration(SUPPLY_CHAIN_ID)).thenReturn(Optional.empty());
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> service.getReleaseConfiguration(SUPPLY_CHAIN_ID));
        assertThat(exception.getMessage(), is("404 NOT_FOUND \"release configuration not found\""));
    }

    @Test
    void createReleaseConfiguration() {
        ResponseEntity<RestReleaseConfiguration> response = service.createReleaseConfiguration(SUPPLY_CHAIN_ID, restReleaseConfiguration);
        verify(layoutMetaBlockService).createReleaseConfiguration(releaseConfiguration);
        assertThat(response.getBody(), is(restReleaseConfiguration));
        assertThat(response.getStatusCode().value(), is(200));
    }

    private static List<Step> createStep() {
        return singletonList(Step.builder()
                .name(STEP_NAME)
                .build());
    }

}
