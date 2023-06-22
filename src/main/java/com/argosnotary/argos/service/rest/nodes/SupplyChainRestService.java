package com.argosnotary.argos.service.rest.nodes;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import com.argosnotary.argos.service.openapi.rest.api.SupplyChainApi;
import com.argosnotary.argos.service.openapi.rest.model.RestSupplyChain;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import jakarta.validation.Valid;

public interface SupplyChainRestService extends SupplyChainApi {

	@Override
	ResponseEntity<RestSupplyChain> createSupplyChain(
			@Parameter(name = "projectId", description = "this will be the project id", required = true, in = ParameterIn.PATH) @PathVariable("projectId") UUID projectId,
			@Parameter(name = "RestSupplyChain", description = "") @Valid @RequestBody(required = false) RestSupplyChain restSupplyChain);

	@Override
	ResponseEntity<Void> deleteSupplyChainById(
			@Parameter(name = "supplyChainId", description = "supply chain id", required = true, in = ParameterIn.PATH) @PathVariable("supplyChainId") UUID supplyChainId);

	@Override
	ResponseEntity<RestSupplyChain> getSupplyChain(
			@Parameter(name = "supplyChainId", description = "supply chain id", required = true, in = ParameterIn.PATH) @PathVariable("supplyChainId") UUID supplyChainId);

	@Override
	ResponseEntity<List<RestSupplyChain>> getSupplyChains();

	@Override
	ResponseEntity<List<RestSupplyChain>> getSupplyChainsForProject(
			@Parameter(name = "projectId", description = "", required = true, in = ParameterIn.PATH) @PathVariable("projectId") UUID projectId);

	@Override
	ResponseEntity<RestSupplyChain> updateSupplyChain(
			@Parameter(name = "supplyChainId", description = "supply chain id", required = true, in = ParameterIn.PATH) @PathVariable("supplyChainId") UUID supplyChainId,
			@Parameter(name = "RestSupplyChain", description = "", required = true) @Valid @RequestBody RestSupplyChain restSupplyChain);

}
