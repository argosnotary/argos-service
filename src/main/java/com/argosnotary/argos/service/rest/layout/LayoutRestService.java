package com.argosnotary.argos.service.rest.layout;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import com.argosnotary.argos.service.openapi.rest.api.LayoutApi;
import com.argosnotary.argos.service.openapi.rest.model.RestApprovalConfiguration;
import com.argosnotary.argos.service.openapi.rest.model.RestLayout;
import com.argosnotary.argos.service.openapi.rest.model.RestLayoutMetaBlock;
import com.argosnotary.argos.service.openapi.rest.model.RestReleaseConfiguration;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

public interface LayoutRestService extends LayoutApi {
	@Override
	public ResponseEntity<List<RestApprovalConfiguration>> createApprovalConfigurations(
			@Parameter(name = "supplyChainId", description = "supply chain id", required = true, in = ParameterIn.PATH) @PathVariable("supplyChainId") UUID supplyChainId,
			@Parameter(name = "RestApprovalConfiguration", description = "", required = true) @Valid @Size(max = 20) @RequestBody List<RestApprovalConfiguration> restApprovalConfiguration);

	@Override
	public ResponseEntity<RestLayoutMetaBlock> createOrUpdateLayout(
			@Parameter(name = "supplyChainId", description = "supply chain id", required = true, in = ParameterIn.PATH) @PathVariable("supplyChainId") UUID supplyChainId,
			@Parameter(name = "RestLayoutMetaBlock", description = "", required = true) @Valid @RequestBody RestLayoutMetaBlock restLayoutMetaBlock);

	@Override
	public ResponseEntity<RestReleaseConfiguration> createReleaseConfiguration(
			@Parameter(name = "supplyChainId", description = "supply chain id", required = true, in = ParameterIn.PATH) @PathVariable("supplyChainId") UUID supplyChainId,
			@Parameter(name = "RestReleaseConfiguration", description = "", required = true) @Valid @RequestBody RestReleaseConfiguration restReleaseConfiguration);

	@Override
	public ResponseEntity<List<RestApprovalConfiguration>> getApprovalConfigurations(
			@Parameter(name = "supplyChainId", description = "supply chain id", required = true, in = ParameterIn.PATH) @PathVariable("supplyChainId") UUID supplyChainId);

	@Override
	public ResponseEntity<List<RestApprovalConfiguration>> getApprovalsForAccount(
			@Parameter(name = "supplyChainId", description = "supply chain id", required = true, in = ParameterIn.PATH) @PathVariable("supplyChainId") UUID supplyChainId);

	@Override
	public ResponseEntity<RestLayoutMetaBlock> getLayout(
			@Parameter(name = "supplyChainId", description = "supply chain id", required = true, in = ParameterIn.PATH) @PathVariable("supplyChainId") UUID supplyChainId);

	@Override
	public ResponseEntity<RestReleaseConfiguration> getReleaseConfiguration(
			@Parameter(name = "supplyChainId", description = "supply chain id", required = true, in = ParameterIn.PATH) @PathVariable("supplyChainId") UUID supplyChainId);

	@Override
	public ResponseEntity<Void> validateLayout(
			@Parameter(name = "supplyChainId", description = "supply chain id", required = true, in = ParameterIn.PATH) @PathVariable("supplyChainId") UUID supplyChainId,
			@Parameter(name = "RestLayout", description = "", required = true) @Valid @RequestBody RestLayout restLayout);

}
