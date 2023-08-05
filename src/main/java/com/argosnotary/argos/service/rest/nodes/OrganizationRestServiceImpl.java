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
package com.argosnotary.argos.service.rest.nodes;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import com.argosnotary.argos.domain.account.Account;
import com.argosnotary.argos.domain.account.PersonalAccount;
import com.argosnotary.argos.domain.nodes.Node;
import com.argosnotary.argos.domain.nodes.Organization;
import com.argosnotary.argos.domain.roles.Permission;
import com.argosnotary.argos.service.account.AccountSecurityContext;
import com.argosnotary.argos.service.auditlog.AuditLog;
import com.argosnotary.argos.service.nodes.NodeService;
import com.argosnotary.argos.service.nodes.OrganizationService;
import com.argosnotary.argos.service.openapi.rest.model.RestOrganization;
import com.argosnotary.argos.service.roles.PermissionCheck;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class OrganizationRestServiceImpl implements OrganizationRestService {

	private final OrganizationService organizationService;
	
	private final NodeService nodeService;

	private final OrganizationMapper organizationMapper;

	private final AccountSecurityContext accountSecurityContext;

	@Override
	@PreAuthorize("isAuthenticated()")
	@Transactional
	@AuditLog
	public ResponseEntity<RestOrganization> createOrganization(RestOrganization restOrganization) {

		Optional<Account> optAccount = accountSecurityContext.getAuthenticatedAccount();

		if (!(optAccount.isPresent() && optAccount.get() instanceof PersonalAccount)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid account");
		}

		if (organizationService.existsByName(restOrganization.getName())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					String.format("Organization with name [%s] already exists", restOrganization.getName()));
		}

		Organization org = organizationService.create(organizationMapper.convertFromRestOrganization(restOrganization));

		URI location = UriComponentsBuilder
        		.fromPath("/api/organizations/{organizationId}")
				.buildAndExpand(org.getId()).toUri();
		return ResponseEntity.created(location).body(organizationMapper.convertToRestOrganization(org));
	}

	@Override
	@PermissionCheck(permissions = Permission.WRITE)
	@Transactional
	@AuditLog
	public ResponseEntity<Void> deleteOrganizationById(UUID organizationId) {
		if (!organizationService.exists(organizationId)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Organization not found");
		}
		organizationService.delete(organizationId);
		return ResponseEntity.noContent().build();
	}

	@Override
	@PermissionCheck(permissions = Permission.READ)
	public ResponseEntity<RestOrganization> getOrganization(UUID organizationId) {
		Organization org = organizationService.findById(organizationId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Organization not found"));
		return ResponseEntity.ok(organizationMapper.convertToRestOrganization(org));
	}

	@Override
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<List<RestOrganization>> getOrganizations(UUID nodeId) {
		Optional<Node> optNode = Optional.empty();
		if (nodeId != null) {
			optNode = nodeService.findById(nodeId);
			if (optNode.isEmpty()) {
				throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Node with id [%s] not found", nodeId));
			}
		}
		return ResponseEntity
				.ok(organizationService.find(optNode).stream().map(organizationMapper::convertToRestOrganization).toList());
	}

}
