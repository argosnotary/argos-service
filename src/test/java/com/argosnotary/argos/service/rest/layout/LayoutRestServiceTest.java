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
package com.argosnotary.argos.service.rest.layout;

import static com.argosnotary.argos.service.openapi.rest.model.RestArtifactCollectorSpecification.TypeEnum.XLDEPLOY;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.hamcrest.core.Is;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;

import com.argosnotary.argos.domain.ArgosError;
import com.argosnotary.argos.domain.account.Account;
import com.argosnotary.argos.domain.crypto.KeyPair;
import com.argosnotary.argos.domain.layout.ApprovalConfiguration;
import com.argosnotary.argos.domain.layout.Layout;
import com.argosnotary.argos.domain.layout.LayoutMetaBlock;
import com.argosnotary.argos.domain.layout.ReleaseConfiguration;
import com.argosnotary.argos.domain.layout.Step;
import com.argosnotary.argos.service.account.AccountSecurityContext;
import com.argosnotary.argos.service.layout.ApprovalConfigurationService;
import com.argosnotary.argos.service.layout.LayoutMetaBlockService;
import com.argosnotary.argos.service.layout.ReleaseConfigurationService;
import com.argosnotary.argos.service.openapi.rest.model.RestApprovalConfiguration;
import com.argosnotary.argos.service.openapi.rest.model.RestArtifactCollectorSpecification;
import com.argosnotary.argos.service.openapi.rest.model.RestLayout;
import com.argosnotary.argos.service.openapi.rest.model.RestLayoutMetaBlock;
import com.argosnotary.argos.service.openapi.rest.model.RestReleaseConfiguration;

import jakarta.servlet.http.HttpServletRequest;

@ExtendWith(MockitoExtension.class)
class LayoutRestServiceTest {

    private static final String SEGMENT_NAME = "segmentName";
    private static final String STEP_NAME = "stepName";
    private static final UUID SUPPLY_CHAIN_ID = UUID.randomUUID();

    @Mock
    private LayoutMetaBlockMapper converter;

    @Mock
    private LayoutMetaBlockService layoutMetaBlockService;

    @Mock
    private RestLayoutMetaBlock restLayoutMetaBlock;

    @Mock
    private ApprovalConfigurationService approvalConfigurationService;

    @Mock
    private ConfigurationMapper configurationMapper;

    @Mock
    private ReleaseConfigurationService releaseConfigurationService;

    @Mock
    private RestLayout restLayout;

    @Mock
    private Layout layout;

    @Mock
    private LayoutMetaBlock layoutMetaBlock;

    @Mock
    private HttpServletRequest httpServletRequest;

    private LayoutRestService service;

    @Mock
    private LayoutValidatorService validator;

    @Mock
    private RestApprovalConfiguration restApprovalConfiguration;

    @Mock
    private ApprovalConfiguration approvalConfiguration;
    @Mock
    private RestArtifactCollectorSpecification restArtifactCollectorSpecification;

    @Mock
    private AccountSecurityContext accountSecurityContext;

    @Mock
    private Account account;

    @Mock
    private KeyPair keyPair;

    @Mock
    private ReleaseConfiguration releaseConfiguration;

    @Mock
    private RestReleaseConfiguration restReleaseConfiguration;

    @Mock
    private Step step;

    @BeforeEach
    void setUp() {
        service = new LayoutRestServiceImpl(converter, layoutMetaBlockService, validator, approvalConfigurationService, releaseConfigurationService, configurationMapper, accountSecurityContext);
    }

    @Test
    void createOrUpdateLayout() {
        ServletRequestAttributes servletRequestAttributes = new ServletRequestAttributes(httpServletRequest);
        RequestContextHolder.setRequestAttributes(servletRequestAttributes);
        when(converter.convertFromRestLayoutMetaBlock(restLayoutMetaBlock)).thenReturn(layoutMetaBlock);
        when(converter.convertToRestLayoutMetaBlock(layoutMetaBlock)).thenReturn(restLayoutMetaBlock);
        ResponseEntity<RestLayoutMetaBlock> responseEntity = service.createOrUpdateLayout(SUPPLY_CHAIN_ID, restLayoutMetaBlock);
        assertThat(responseEntity.getStatusCodeValue(), is(201));
        assertThat(responseEntity.getBody(), sameInstance(restLayoutMetaBlock));
        assertThat(Objects.requireNonNull(responseEntity.getHeaders().getLocation()).getPath(), is(""));
        verify(layoutMetaBlockService).save(layoutMetaBlock);
        verify(validator).validate(layoutMetaBlock);

    }

    @Test
    void validateLayoutValid() {
        when(converter.convertFromRestLayout(restLayout)).thenReturn(layout);
        ResponseEntity responseEntity = service.validateLayout(SUPPLY_CHAIN_ID, restLayout);
        assertThat(responseEntity.getStatusCodeValue(), is(204));
        verify(validator).validateLayout(layout);
    }

    @Test
    void getLayout() {
        when(converter.convertToRestLayoutMetaBlock(layoutMetaBlock)).thenReturn(restLayoutMetaBlock);
        when(layoutMetaBlockService.findBySupplyChainId(SUPPLY_CHAIN_ID)).thenReturn(Optional.of(layoutMetaBlock));
        ResponseEntity<RestLayoutMetaBlock> responseEntity = service.getLayout(SUPPLY_CHAIN_ID);
        assertThat(responseEntity.getStatusCodeValue(), is(200));
        assertThat(responseEntity.getBody(), sameInstance(restLayoutMetaBlock));
    }

    @Test
    void getLayoutNotFound() {
        when(layoutMetaBlockService.findBySupplyChainId(SUPPLY_CHAIN_ID)).thenReturn(Optional.empty());
        ResponseStatusException responseStatusException = assertThrows(ResponseStatusException.class, () -> service.getLayout(SUPPLY_CHAIN_ID));
        assertThat(responseStatusException.getStatusCode(), is(HttpStatus.NOT_FOUND));
        assertThat(responseStatusException.getReason(), is("layout not found"));
    }

    @Test
    void createApprovalConfigurationShouldStoreLayout() {
        when(layoutMetaBlockService.findBySupplyChainId(SUPPLY_CHAIN_ID)).thenReturn(Optional.of(layoutMetaBlock));
        when(approvalConfiguration.getSupplyChainId()).thenReturn(SUPPLY_CHAIN_ID);
        when(layoutMetaBlock.getLayout()).thenReturn(layout);
        when(layout.getSteps()).thenReturn(createStep());
        when(approvalConfiguration.getStepName()).thenReturn(STEP_NAME);
        when(configurationMapper.convertFromRestApprovalConfiguration(restApprovalConfiguration))
                .thenReturn(approvalConfiguration);
        when(configurationMapper.convertToRestApprovalConfiguration(approvalConfiguration))
                .thenReturn(restApprovalConfiguration);
        ResponseEntity<List<RestApprovalConfiguration>> responseEntity = service.
                createApprovalConfigurations(SUPPLY_CHAIN_ID, List.of(restApprovalConfiguration));
        assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
        verify(approvalConfigurationService).save(SUPPLY_CHAIN_ID, List.of(approvalConfiguration));
        verify(approvalConfiguration).setSupplyChainId(SUPPLY_CHAIN_ID);
    }

    @Test
    void createApprovalConfigurationWithIncorrectStepNameShouldThrowValidationError() {
        when(layoutMetaBlockService.findBySupplyChainId(SUPPLY_CHAIN_ID)).thenReturn(Optional.of(layoutMetaBlock));
        when(approvalConfiguration.getSupplyChainId()).thenReturn(SUPPLY_CHAIN_ID);
        when(approvalConfiguration.getStepName()).thenReturn("wrong-step");
        when(layoutMetaBlock.getLayout()).thenReturn(layout);
        when(layout.getSteps()).thenReturn(createStep());
        when(configurationMapper.convertFromRestApprovalConfiguration(restApprovalConfiguration))
                .thenReturn(approvalConfiguration);
        List<RestApprovalConfiguration> configs = List.of(restApprovalConfiguration);
        LayoutValidationException layoutValidationException = assertThrows(LayoutValidationException.class, () ->
                service.createApprovalConfigurations(SUPPLY_CHAIN_ID, configs)
        );

        assertThat(layoutValidationException.getValidationMessages().isEmpty(), is(false));
        assertThat(layoutValidationException.getValidationMessages().get(0).getField(), is("stepName"));
        assertThat(layoutValidationException.getValidationMessages().get(0).getMessage(), is("step with name: wrong-step does not exist in layout"));
    }

    @Test
    void createApprovalConfigurationsWithIncorrectStepNameShouldThrowValidationError() {
        when(layoutMetaBlockService.findBySupplyChainId(SUPPLY_CHAIN_ID)).thenReturn(Optional.of(layoutMetaBlock));
        when(approvalConfiguration.getSupplyChainId()).thenReturn(SUPPLY_CHAIN_ID);

        when(layoutMetaBlock.getLayout()).thenReturn(layout);
        when(layout.getSteps()).thenReturn(createStep());
        when(approvalConfiguration.getStepName()).thenReturn("wrong-stepname");
        when(configurationMapper.convertFromRestApprovalConfiguration(restApprovalConfiguration))
                .thenReturn(approvalConfiguration);
        List<RestApprovalConfiguration> configs = List.of(restApprovalConfiguration);

        LayoutValidationException layoutValidationException = assertThrows(LayoutValidationException.class, () -> service.createApprovalConfigurations(SUPPLY_CHAIN_ID, configs));

        assertThat(layoutValidationException.getValidationMessages().isEmpty(), is(false));
        assertThat(layoutValidationException.getValidationMessages().get(0).getField(), is("stepName"));
        assertThat(layoutValidationException.getValidationMessages().get(0).getMessage(), is("step with name: wrong-stepname does not exist in layout"));
    }


    @Test
    void createApprovalConfigurationsWithoutExistingLayoutShouldThrowValidationError() {
        when(layoutMetaBlockService.findBySupplyChainId(SUPPLY_CHAIN_ID)).thenReturn(Optional.empty());
        when(approvalConfiguration.getSupplyChainId()).thenReturn(SUPPLY_CHAIN_ID);
        when(configurationMapper.convertFromRestApprovalConfiguration(restApprovalConfiguration))
                .thenReturn(approvalConfiguration);
        List<RestApprovalConfiguration> configs = List.of(restApprovalConfiguration);

        ResponseStatusException responseStatusException = assertThrows(ResponseStatusException.class, () ->
                service.createApprovalConfigurations(SUPPLY_CHAIN_ID, configs)
        );
        assertThat(responseStatusException.getStatusCode(), is(HttpStatus.NOT_FOUND));
        assertThat(responseStatusException.getMessage(), is("404 NOT_FOUND \"layout not found\""));
    }


    @Test
    void createApprovalConfigurationsWithIncorrectArtifactSpecificationShouldThrowValidationError() {
        when(restArtifactCollectorSpecification.getContext()).thenReturn(emptyMap());
        when(restArtifactCollectorSpecification.getType()).thenReturn(XLDEPLOY);
        when(restApprovalConfiguration.getArtifactCollectorSpecifications()).thenReturn(singletonList(restArtifactCollectorSpecification));
        List<RestApprovalConfiguration> configs = List.of(restApprovalConfiguration);
        
        LayoutValidationException layoutValidationException = assertThrows(LayoutValidationException.class, () -> service.createApprovalConfigurations(SUPPLY_CHAIN_ID, configs));
        assertThat(layoutValidationException.getValidationMessages().isEmpty(), is(false));
        assertThat(layoutValidationException.getValidationMessages().get(0).getField(), is("context"));
        assertThat(layoutValidationException.getValidationMessages().get(0).getMessage(), is("required fields : [applicationName] not present for collector type: XLDEPLOY"));
    }


    @Test
    void getApprovalConfigurations() {
        when(approvalConfigurationService.findBySupplyChainId(SUPPLY_CHAIN_ID)).thenReturn(List.of(approvalConfiguration));
        when(configurationMapper.convertToRestApprovalConfiguration(approvalConfiguration))
                .thenReturn(restApprovalConfiguration);
        ResponseEntity<List<RestApprovalConfiguration>> responseEntity = service.getApprovalConfigurations(SUPPLY_CHAIN_ID);
        assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
        assertThat(responseEntity.getBody(), hasSize(1));
    }

    @Test
    void getApprovalsForAccount() {
        when(layoutMetaBlockService.findBySupplyChainId(SUPPLY_CHAIN_ID)).thenReturn(Optional.of(layoutMetaBlock));
        when(layoutMetaBlock.getLayout()).thenReturn(layout);
        when(layout.getSteps()).thenReturn(List.of(step));


        when(accountSecurityContext.getAuthenticatedAccount()).thenReturn(Optional.of(account));
        when(account.getActiveKeyPair()).thenReturn(keyPair);

        when(approvalConfigurationService.findBySupplyChainId(SUPPLY_CHAIN_ID)).thenReturn(List.of(approvalConfiguration));
        when(configurationMapper.convertToRestApprovalConfiguration(approvalConfiguration))
                .thenReturn(restApprovalConfiguration);


        when(approvalConfiguration.getStepName()).thenReturn("step1");
        when(keyPair.getKeyId()).thenReturn("accountKeyId");

        when(step.getName()).thenReturn("step1");
        when(step.getAuthorizedKeyIds()).thenReturn(List.of("accountKeyId"));

        ResponseEntity<List<RestApprovalConfiguration>> responseEntity = service.getApprovalsForAccount(SUPPLY_CHAIN_ID);
        assertThat(responseEntity.getStatusCodeValue(), is(200));
        assertThat(responseEntity.getBody(), contains(restApprovalConfiguration));

        when(step.getName()).thenReturn("step1");
        when(step.getAuthorizedKeyIds()).thenReturn(List.of("otherAccountKeyId"));
        assertThat(service.getApprovalsForAccount(SUPPLY_CHAIN_ID).getBody(), empty());

        when(step.getName()).thenReturn("step2");
        assertThat(service.getApprovalsForAccount(SUPPLY_CHAIN_ID).getBody(), empty());


        assertThat(service.getApprovalsForAccount(SUPPLY_CHAIN_ID).getBody(), empty());
    }

    @Test
    void getApprovalsForAccountNoLayout() {
        when(layoutMetaBlockService.findBySupplyChainId(SUPPLY_CHAIN_ID)).thenReturn(Optional.empty());
        when(accountSecurityContext.getAuthenticatedAccount()).thenReturn(Optional.of(account));
        when(account.getActiveKeyPair()).thenReturn(keyPair);
        ResponseEntity<List<RestApprovalConfiguration>> responseEntity = service.getApprovalsForAccount(SUPPLY_CHAIN_ID);
        assertThat(responseEntity.getStatusCodeValue(), is(200));
        assertThat(responseEntity.getBody(), empty());

    }

    @Test
    void getApprovalsForAccountNoAccount() {
        when(accountSecurityContext.getAuthenticatedAccount()).thenReturn(Optional.empty());
        ArgosError exception = assertThrows(ArgosError.class, () -> service.getApprovalsForAccount(SUPPLY_CHAIN_ID));
        assertThat(exception.getMessage(), Is.is("not logged in"));
    }

    @Test
    void getReleaseConfiguration() {
        when(releaseConfigurationService.findBySupplyChainId(SUPPLY_CHAIN_ID)).thenReturn(Optional.of(releaseConfiguration));
        when(configurationMapper.convertToRestReleaseConfiguration(releaseConfiguration)).thenReturn(restReleaseConfiguration);
        ResponseEntity<RestReleaseConfiguration> response = service.getReleaseConfiguration(SUPPLY_CHAIN_ID);
        assertThat(response.getBody(), is(restReleaseConfiguration));
        assertThat(response.getStatusCodeValue(), is(200));
    }

    @Test
    void getReleaseConfigurationNotFound() {
        when(releaseConfigurationService.findBySupplyChainId(SUPPLY_CHAIN_ID)).thenReturn(Optional.empty());
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> service.getReleaseConfiguration(SUPPLY_CHAIN_ID));
        assertThat(exception.getMessage(), is("404 NOT_FOUND \"release configuration not found\""));
    }

    @Test
    void createReleaseConfiguration() {
        when(configurationMapper.convertFromRestReleaseConfiguration(restReleaseConfiguration)).thenReturn(releaseConfiguration);
        ResponseEntity<RestReleaseConfiguration> response = service.createReleaseConfiguration(SUPPLY_CHAIN_ID, restReleaseConfiguration);
        verify(releaseConfigurationService).save(releaseConfiguration);
        verify(releaseConfiguration).setSupplyChainId(SUPPLY_CHAIN_ID);
        assertThat(response.getBody(), is(restReleaseConfiguration));
        assertThat(response.getStatusCodeValue(), is(200));
    }

    private static List<Step> createStep() {
        return singletonList(Step.builder()
                .name(STEP_NAME)
                .build());
    }

}
