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
package com.argosnotary.argos.service.nodes;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.argosnotary.argos.domain.account.PersonalAccount;
import com.argosnotary.argos.domain.nodes.Node;
import com.argosnotary.argos.domain.nodes.Organization;
import com.argosnotary.argos.domain.roles.Role;
import com.argosnotary.argos.service.account.AccountSecurityContext;
import com.argosnotary.argos.service.roles.RoleAssignmentService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrganizationServiceImpl implements OrganizationService {
	
	private final NodeService nodeService;
	
	private final RoleAssignmentService roleAssignmentService;

    private final AccountSecurityContext accountSecurityContext;

	@Override
	public List<Organization> find() {
		return nodeService.find(Organization.class.getCanonicalName(), Optional.empty())
				.stream().map(n -> (Organization) n).toList();
	}

	@Override
	public Optional<Organization> findById(UUID organizationID) {
		Optional<Node> node = nodeService.findById(organizationID);
		if (node.isEmpty() || ! (node.get() instanceof Organization)) {
			return Optional.empty();
		}
		return Optional.of((Organization) node.get());
	}

	@Override
	public Organization create(Organization organization) {
		// user is authenticated with PersonalAccount
		// validated in Rest Service
		PersonalAccount account = (PersonalAccount)accountSecurityContext.getAuthenticatedAccount().orElseThrow();
		Organization newOrg = (Organization) nodeService.create(organization);
		// creator becomes Owner
		roleAssignmentService.create(newOrg.getId(), account.getId(), new Role.Owner());
		return newOrg;
	}

	@Override
	public void delete(UUID organizationId) {
		nodeService.delete(organizationId);
	}

	@Override
	public boolean exists(UUID organizationId) {
		return nodeService.exists(Organization.class, organizationId);
	}

	@Override
	public boolean existsByName(String name) {
		return nodeService.exists(Organization.class, name);
	}

}
