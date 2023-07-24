/*
 * Argos Notary - A new way to secure the Software Supply Chain
 *
 * Copyright (C) 2019 - 2020 Rabobank Nederland
 * Copyright (C) 2019 - 2023 Gerard Borst <gerard.borst@argosnotary.com>
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
import static org.springframework.http.ResponseEntity.ok;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.argosnotary.argos.domain.layout.ApprovalConfiguration;
import com.argosnotary.argos.domain.layout.Layout;
import com.argosnotary.argos.domain.layout.LayoutMetaBlock;
import com.argosnotary.argos.domain.layout.ReleaseConfiguration;
import com.argosnotary.argos.domain.roles.Permission;
import com.argosnotary.argos.service.auditlog.AuditLog;
import com.argosnotary.argos.service.layout.LayoutMetaBlockService;
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
public class LayoutMetaBlockRestServiceImpl implements LayoutMetaBlockRestService {

    private final LayoutMetaBlockMapper layoutMetaBlockConverter;
    private final LayoutMetaBlockService layoutMetaBlockService;
    private final LayoutValidatorService validator;
    private final ApprovalConfigurationMapper approvalConfigurationMapper;
    private final ReleaseConfigurationMapper releaseConfigurationMapper;


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
        return layoutMetaBlockService.getLayout(supplyChainId)
                .map(layoutMetaBlockConverter::convertToRestLayoutMetaBlock)
                .map(ResponseEntity::ok).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "layout not found"));
    }

    @Override
    @PermissionCheck(permissions = Permission.WRITE)
    public ResponseEntity<List<RestApprovalConfiguration>> createApprovalConfigurations(UUID supplyChainId, 
            List<RestApprovalConfiguration> restApprovalConfigurations) {
        List<ApprovalConfiguration> approvalConfigurations = restApprovalConfigurations.stream()
                .map(restApprovalConfiguration -> convertAndValidate(supplyChainId, restApprovalConfiguration))
                .collect(Collectors.toList());
        approvalConfigurations = layoutMetaBlockService.createApprovalConfigurations(approvalConfigurations);
        return ResponseEntity.ok(approvalConfigurations.stream()
                .map(approvalConfigurationMapper::convertToRestApprovalConfiguration)
                .collect(Collectors.toList()));

    }

    @Override
    @PermissionCheck(permissions = Permission.READ)
    public ResponseEntity<List<RestApprovalConfiguration>> getApprovalConfigurations(UUID supplyChainId) {
        return ResponseEntity.ok(layoutMetaBlockService.getApprovalConfigurations(supplyChainId)
                .stream()
                .map(approvalConfigurationMapper::convertToRestApprovalConfiguration)
                .collect(Collectors.toList()));
    }

    @Override
    @PermissionCheck(permissions = Permission.LINK_ADD)
    public ResponseEntity<List<RestApprovalConfiguration>> getApprovalsForAccount(UUID supplyChainId) {
        return ok(layoutMetaBlockService.getApprovalsForAccount(supplyChainId).stream().map(approvalConfigurationMapper::convertToRestApprovalConfiguration).collect(Collectors.toList()));
    }

    @Override
    @Transactional
    @PermissionCheck(permissions = Permission.WRITE)
    public ResponseEntity<RestReleaseConfiguration> createReleaseConfiguration(UUID supplyChainId, 
            RestReleaseConfiguration restReleaseConfiguration) {
        validateContextFieldsForCollectorSpecification(restReleaseConfiguration);
        ReleaseConfiguration releaseConfiguration = releaseConfigurationMapper.convertFromRestReleaseConfiguration(restReleaseConfiguration);
        releaseConfiguration.setSupplyChainId(supplyChainId);
        layoutMetaBlockService.createReleaseConfiguration(releaseConfiguration);
        return ResponseEntity.ok(restReleaseConfiguration);
    }

    @Override
    @PermissionCheck(permissions = Permission.READ)
    public ResponseEntity<RestReleaseConfiguration> getReleaseConfiguration(UUID supplyChainId) {
        return ResponseEntity.ok(layoutMetaBlockService.getReleaseConfiguration(supplyChainId)
                .map(releaseConfigurationMapper::convertToRestReleaseConfiguration)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "release configuration not found")));
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
        ApprovalConfiguration approvalConfiguration = approvalConfigurationMapper.convertFromRestApprovalConfiguration(restApprovalConfiguration);
        approvalConfiguration.setSupplyChainId(supplyChainId);
        verifyStepNameExistInLayout(approvalConfiguration);
        return approvalConfiguration;
    }

    private void verifyStepNameExistInLayout(ApprovalConfiguration approvalConfiguration) {
    	LayoutMetaBlock layoutMetaBlock = layoutMetaBlockService.getLayout(approvalConfiguration.getSupplyChainId())
    			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "layout not found"));
    	
        if (!layoutMetaBlockService.stepNameExistInLayout(layoutMetaBlock.getLayout(), approvalConfiguration.getStepName())) {
            throwLayoutValidationException(
                    MODEL_CONSISTENCY,
                    "stepName",
                    "step with name: " + approvalConfiguration.getStepName() + " does not exist in layout"
            );
        }
    }
}
