package com.argosnotary.argos.service.rest.nodes;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.argosnotary.argos.domain.account.Account;
import com.argosnotary.argos.domain.account.PersonalAccount;
import com.argosnotary.argos.domain.nodes.Organization;
import com.argosnotary.argos.domain.roles.Permission;
import com.argosnotary.argos.service.account.AccountSecurityContext;
import com.argosnotary.argos.service.auditlog.AuditLog;
import com.argosnotary.argos.service.nodes.OrganizationService;
import com.argosnotary.argos.service.openapi.rest.model.RestOrganization;
import com.argosnotary.argos.service.roles.PermissionCheck;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class OrganizationRestServiceImpl implements OrganizationRestService {
	
	private final OrganizationService organizationService;
	
	private final OrganizationMapper organizationMapper;

    private final AccountSecurityContext accountSecurityContext;
	
	@Override
    @PreAuthorize("isAuthenticated()")
    @Transactional
    @AuditLog
    public ResponseEntity<RestOrganization> createOrganization(
	        @Parameter(name = "RestOrganization", description = "") @Valid @RequestBody(required = false) RestOrganization restOrganization
		    ) {
		
		Optional<Account> optAccount = accountSecurityContext.getAuthenticatedAccount();
		
		if (! (optAccount.isPresent() && optAccount.get() instanceof PersonalAccount)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid account"); 
		}
		
		if (organizationService.existsByName(restOrganization.getName())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Organization with name [%s] already exists", restOrganization.getName()));
		}
		
		Organization org = organizationService
				.create(organizationMapper.convertFromRestOrganization(restOrganization));

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{organizationId}")
                .buildAndExpand(org.getId())
                .toUri();
		return ResponseEntity.created(location).body(organizationMapper.convertToRestOrganization(org));
	}
	

	@Override
    @PermissionCheck(permissions = Permission.WRITE)
    @Transactional
    @AuditLog
	public ResponseEntity<Void> deleteOrganizationById(
	        @Parameter(name = "organizationId", description = "organization id", required = true, in = ParameterIn.PATH) @PathVariable("organizationId") UUID organizationId
		    ) {
		if (!organizationService.existsById(organizationId)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Organization not found");
		}
		organizationService.delete(organizationId);
		return ResponseEntity.noContent().build();
	}
	
	@Override
    @PermissionCheck(permissions = Permission.READ)
	public ResponseEntity<RestOrganization> getOrganization(
	        @Parameter(name = "organizationId", description = "this will be the organizationId id", required = true, in = ParameterIn.PATH) @PathVariable("organizationId") UUID organizationId
	    ) {
		Organization org = organizationService.findById(organizationId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Organization not found"));
		return ResponseEntity.ok(organizationMapper.convertToRestOrganization(org));
	}
	
	@Override
    @PreAuthorize("isAuthenticated()")
	public  ResponseEntity<List<RestOrganization>> getOrganizations(
	        
		    ) {
		return ResponseEntity.ok(organizationService.find()
				.stream()
				.map(organizationMapper::convertToRestOrganization)
				.collect(Collectors.toList()));
	}

}
