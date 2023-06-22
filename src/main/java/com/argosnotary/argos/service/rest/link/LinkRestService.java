package com.argosnotary.argos.service.rest.link;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.argosnotary.argos.service.openapi.rest.api.LinkApi;
import com.argosnotary.argos.service.openapi.rest.model.RestLinkMetaBlock;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public interface LinkRestService extends LinkApi {
	@Override
	public ResponseEntity<Void> createLink(
			@Parameter(name = "supplyChainId", description = "supply chain id", required = true, in = ParameterIn.PATH) @PathVariable("supplyChainId") UUID supplyChainId,
			@Parameter(name = "RestLinkMetaBlock", description = "") @Valid @RequestBody(required = false) RestLinkMetaBlock restLinkMetaBlock);

	@Override
	public ResponseEntity<List<RestLinkMetaBlock>> findLink(
			@Parameter(name = "supplyChainId", description = "supply chain id", required = true, in = ParameterIn.PATH) @PathVariable("supplyChainId") UUID supplyChainId,
			@Pattern(regexp = "^[0-9a-f]*$") @Size(min = 64, max = 64) @Parameter(name = "hash", description = "hash of product or material", in = ParameterIn.QUERY) @Valid @RequestParam(value = "hash", required = false) String hash);
}
