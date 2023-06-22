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

import static com.argosnotary.argos.service.openapi.rest.model.RestValidationMessage.TypeEnum.MODEL_CONSISTENCY;
import static com.argosnotary.argos.service.rest.layout.ValidationHelper.throwLayoutValidationException;
import static java.util.Collections.emptyList;
import static org.springframework.http.ResponseEntity.ok;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.argosnotary.argos.domain.ArgosError;
import com.argosnotary.argos.domain.account.Account;
import com.argosnotary.argos.domain.crypto.KeyPair;
import com.argosnotary.argos.domain.layout.ApprovalConfiguration;
import com.argosnotary.argos.domain.layout.Layout;
import com.argosnotary.argos.domain.layout.LayoutMetaBlock;
import com.argosnotary.argos.domain.layout.ReleaseConfiguration;
import com.argosnotary.argos.domain.layout.Step;
import com.argosnotary.argos.domain.roles.Permission;
import com.argosnotary.argos.service.account.AccountSecurityContext;
import com.argosnotary.argos.service.auditlog.AuditLog;
import com.argosnotary.argos.service.layout.ApprovalConfigurationService;
import com.argosnotary.argos.service.layout.LayoutMetaBlockService;
import com.argosnotary.argos.service.layout.ReleaseConfigurationService;
import com.argosnotary.argos.service.openapi.rest.model.RestApprovalConfiguration;
import com.argosnotary.argos.service.openapi.rest.model.RestArtifactCollectorSpecification;
import com.argosnotary.argos.service.openapi.rest.model.RestLayout;
import com.argosnotary.argos.service.openapi.rest.model.RestLayoutMetaBlock;
import com.argosnotary.argos.service.openapi.rest.model.RestReleaseConfiguration;
import com.argosnotary.argos.service.roles.PermissionCheck;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api")
public class LayoutRestServiceImpl implements LayoutRestService {

    private final LayoutMetaBlockMapper layoutMetaBlockConverter;
    private final LayoutMetaBlockService layoutMetaBlockService;
    private final LayoutValidatorService validator;
    private final ApprovalConfigurationService approvalConfigurationService;
    private final ReleaseConfigurationService releaseConfigurationService;
    private final ConfigurationMapper configurationMapper;
    private final AccountSecurityContext accountSecurityContext;


    @Override
    @PermissionCheck(permissions = Permission.WRITE)
    public ResponseEntity<Void> validateLayout(UUID supplyChainId, RestLayout restLayout) {
        Layout layout = layoutMetaBlockConverter.convertFromRestLayout(restLayout);
        validator.validateLayout(layout);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Override
    @PermissionCheck(permissions = Permission.WRITE)
    @AuditLog
    @Transactional
    public ResponseEntity<RestLayoutMetaBlock> createOrUpdateLayout(UUID supplyChainId, RestLayoutMetaBlock restLayoutMetaBlock) {
        log.info("createLayout for supplyChainId {}", supplyChainId);
        LayoutMetaBlock layoutMetaBlock = layoutMetaBlockConverter.convertFromRestLayoutMetaBlock(restLayoutMetaBlock);
        layoutMetaBlock.setSupplyChainId(supplyChainId);
        validator.validate(layoutMetaBlock);
        layoutMetaBlockService.save(layoutMetaBlock);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().build().toUri();
        return ResponseEntity.created(location).body(layoutMetaBlockConverter.convertToRestLayoutMetaBlock(layoutMetaBlock));
    }

    @Override
    @PermissionCheck(permissions = Permission.READ)
    public ResponseEntity<RestLayoutMetaBlock> getLayout(UUID supplyChainId) {
        return layoutMetaBlockService.findBySupplyChainId(supplyChainId)
                .map(layoutMetaBlockConverter::convertToRestLayoutMetaBlock)
                .map(ResponseEntity::ok).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "layout not found"));
    }

    @Override
    @Transactional
    @PermissionCheck(permissions = Permission.WRITE)
    public ResponseEntity<List<RestApprovalConfiguration>> createApprovalConfigurations(UUID supplyChainId, 
            List<RestApprovalConfiguration> restApprovalConfigurations) {
        List<ApprovalConfiguration> approvalConfigurations = restApprovalConfigurations.stream()
                .map(restApprovalConfiguration -> convertAndValidate(supplyChainId, restApprovalConfiguration))
                .collect(Collectors.toList());
        approvalConfigurationService.save(supplyChainId, approvalConfigurations);
        return ResponseEntity.ok(approvalConfigurations.stream()
                .map(configurationMapper::convertToRestApprovalConfiguration)
                .collect(Collectors.toList()));

    }

    @Override
    @PermissionCheck(permissions = Permission.READ)
    public ResponseEntity<List<RestApprovalConfiguration>> getApprovalConfigurations(UUID supplyChainId) {
        return ResponseEntity.ok(approvalConfigurationService
                .findBySupplyChainId(supplyChainId)
                .stream()
                .map(configurationMapper::convertToRestApprovalConfiguration)
                .collect(Collectors.toList()));
    }

    @Override
    @PermissionCheck(permissions = Permission.LINK_ADD)
    public ResponseEntity<List<RestApprovalConfiguration>> getApprovalsForAccount(UUID supplyChainId) {

        Account account = accountSecurityContext.getAuthenticatedAccount().orElseThrow(() -> new ArgosError("not logged in"));

        Optional<KeyPair> optionalKeyPair = Optional.ofNullable(account.getActiveKeyPair());
        Optional<LayoutMetaBlock> optionalLayoutMetaBlock = layoutMetaBlockService.findBySupplyChainId(supplyChainId);

        if (optionalKeyPair.isPresent() && optionalLayoutMetaBlock.isPresent()) {
            String activeAccountKeyId = optionalKeyPair.get().getKeyId();
            Layout layout = optionalLayoutMetaBlock.get().getLayout();
            return ok(approvalConfigurationService.findBySupplyChainId(supplyChainId).stream().filter(approvalConf -> canApprove(approvalConf, activeAccountKeyId, layout)
            ).map(configurationMapper::convertToRestApprovalConfiguration).collect(Collectors.toList()));
        } else {
            return ok(emptyList());
        }
    }

    @Override
    @Transactional
    @PermissionCheck(permissions = Permission.WRITE)
    public ResponseEntity<RestReleaseConfiguration> createReleaseConfiguration(UUID supplyChainId, 
            RestReleaseConfiguration restReleaseConfiguration) {
        validateContextFieldsForCollectorSpecification(restReleaseConfiguration);
        ReleaseConfiguration releaseConfiguration = configurationMapper.convertFromRestReleaseConfiguration(restReleaseConfiguration);
        releaseConfiguration.setSupplyChainId(supplyChainId);
        releaseConfigurationService.save(releaseConfiguration);
        return ResponseEntity.ok(restReleaseConfiguration);
    }

    @Override
    @PermissionCheck(permissions = Permission.READ)
    public ResponseEntity<RestReleaseConfiguration> getReleaseConfiguration(UUID supplyChainId) {
        return ResponseEntity.ok(releaseConfigurationService.findBySupplyChainId(supplyChainId)
                .map(configurationMapper::convertToRestReleaseConfiguration)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "release configuration not found")));
    }
    
    private boolean canApprove(ApprovalConfiguration approvalConf, String activeAccountKeyId, Layout layout) {
        Optional<Boolean> canApprove = layout.getSteps().stream()
                .filter(step -> step.getName().equals(approvalConf.getStepName()))
                .map(step -> step.getAuthorizedKeyIds().contains(activeAccountKeyId)).findFirst();
        return canApprove.isPresent() && canApprove.get();
    }

    private void validateContextFieldsForCollectorSpecification(RestApprovalConfiguration approvalConfiguration) {
        approvalConfiguration.getArtifactCollectorSpecifications()
                .forEach(this::validateContextFieldsForCollectorSpecification);
    }

    private void validateContextFieldsForCollectorSpecification(RestReleaseConfiguration restReleaseConfiguration) {
        restReleaseConfiguration.getArtifactCollectorSpecifications()
                .forEach(this::validateContextFieldsForCollectorSpecification);
    }

    private void validateContextFieldsForCollectorSpecification(RestArtifactCollectorSpecification restArtifactCollectorSpecification) {
        ContextInputValidator.of(restArtifactCollectorSpecification.getType()).validateContextFields(restArtifactCollectorSpecification);
    }

    private ApprovalConfiguration convertAndValidate(UUID supplyChainId, RestApprovalConfiguration restApprovalConfiguration) {
        validateContextFieldsForCollectorSpecification(restApprovalConfiguration);
        ApprovalConfiguration approvalConfiguration = configurationMapper.convertFromRestApprovalConfiguration(restApprovalConfiguration);
        approvalConfiguration.setSupplyChainId(supplyChainId);
        verifyStepNameExistInLayout(approvalConfiguration);
        return approvalConfiguration;
    }

    private void verifyStepNameExistInLayout(ApprovalConfiguration approvalConfiguration) {
        Set<String> stepNames = getSteps(approvalConfiguration);
        if (!stepNames.contains(approvalConfiguration.getStepName())) {
            throwLayoutValidationException(
                    MODEL_CONSISTENCY,
                    "stepName",
                    "step with name: " + approvalConfiguration.getStepName() + " does not exist in layout"
            );
        }
    }

    private Set<String> getSteps(ApprovalConfiguration approvalConfiguration) {
        return layoutMetaBlockService.findBySupplyChainId(approvalConfiguration.getSupplyChainId())
                .map(layoutMetaBlock -> layoutMetaBlock
                    .getLayout().getSteps()
                    .stream()
                    .map(Step::getName)
                    .collect(Collectors.toSet())
                )
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "layout not found"));
    }
}
