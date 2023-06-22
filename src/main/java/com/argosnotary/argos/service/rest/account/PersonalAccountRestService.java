package com.argosnotary.argos.service.rest.account;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import com.argosnotary.argos.service.openapi.rest.api.PersonalAccountApi;
import com.argosnotary.argos.service.openapi.rest.model.RestKeyPair;
import com.argosnotary.argos.service.openapi.rest.model.RestPersonalAccount;
import com.argosnotary.argos.service.openapi.rest.model.RestPublicKey;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import jakarta.validation.Valid;

public interface PersonalAccountRestService extends PersonalAccountApi {

	@Override
	ResponseEntity<Void> createKey(
			@Parameter(name = "RestKeyPair", description = "", required = true) @Valid @RequestBody RestKeyPair restKeyPair);

	@Override
	ResponseEntity<RestKeyPair> getKeyPair(

	);

	@Override
	ResponseEntity<RestPersonalAccount> getPersonalAccountById(
			@Parameter(name = "accountId", description = "", required = true, in = ParameterIn.PATH) @PathVariable("accountId") UUID accountId);

	@Override
	ResponseEntity<RestPublicKey> getPersonalAccountKeyById(
			@Parameter(name = "accountId", description = "", required = true, in = ParameterIn.PATH) @PathVariable("accountId") UUID accountId);

	@Override
	ResponseEntity<RestPersonalAccount> whoAmI(

	);

}
