/*
 * Argos Notary - A new way to secure the Software Supply Chain
 *
 * Copyright (C) 2019 - 2020 Rabobank Nederland
 * Copyright (C) 2019 - 2024 Gerard Borst <gerard.borst@argosnotary.com>
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
package com.argosnotary.argos.service.roles;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.argosnotary.argos.domain.account.Account;
import com.argosnotary.argos.domain.account.ServiceAccount;
import com.argosnotary.argos.domain.nodes.Node;
import com.argosnotary.argos.domain.roles.Permission;
import com.argosnotary.argos.domain.roles.Role;
import com.argosnotary.argos.domain.roles.RoleAssignment;
import com.argosnotary.argos.service.account.AccountSecurityContext;
import com.argosnotary.argos.service.mongodb.roles.RoleAssignmentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoleAssignmentServiceImpl implements RoleAssignmentService {
	
	private final RoleAssignmentRepository roleAssignmentRepository;
	
	private final AccountSecurityContext accountSecurityContext;
	

	@Override
	public RoleAssignment save(RoleAssignment roleAssignment) {
		if (roleAssignment.getId() == null) {
			roleAssignment.setId(UUID.randomUUID());
		}
		return roleAssignmentRepository.save(roleAssignment);
	}

	@Override
	public void delete(UUID roleAssignmentId) {
		roleAssignmentRepository.deleteById(roleAssignmentId);
	}

	@Override
	public void deleteByResourceId(UUID resourceId) {
		roleAssignmentRepository.deleteByResourceId(resourceId);
	}

	@Override
	public List<RoleAssignment> findByResourceId(UUID resourceId) {
		return roleAssignmentRepository.findByResourceId(resourceId);
	}

	@Override
	public RoleAssignment create(UUID resourceId, UUID accountId, Role role) {
		RoleAssignment ra = RoleAssignment.builder()
				.identityId(accountId)
				.resourceId(resourceId)
				.role(role).build();
		return save(ra);
	}

	@Override
	public List<RoleAssignment> findByIdentity() {
		Optional<Account> optAccount  = accountSecurityContext.getAuthenticatedAccount();
		if (optAccount.isEmpty()) {
			return List.of();
		}
		// authenticated
		return roleAssignmentRepository.findByIdentityId(optAccount.get().getId());
	}

	@Override
	public Set<Permission> findAllPermissionDownTree(Node node) {

		Optional<Account> optAccount  = accountSecurityContext.getAuthenticatedAccount();
		if (optAccount.isEmpty()) {
			return Set.of();
		}
		List<UUID> pathResourceIds = node.getPathToRoot();

		if (optAccount.get() instanceof ServiceAccount sa) {
			return (sa.getRoleAssignments().stream()
					.filter(ra -> pathResourceIds.contains(ra.getResourceId()))
					.map(rs -> rs.getRole().getPermissions()))
					.flatMap(Set::stream)
					.collect(Collectors.toSet());
		}
		
		// account is personal account
		return roleAssignmentRepository.findByResourceIdsAndIdentityId(pathResourceIds, optAccount.get().getId())
			.stream()
			.map(ras -> ras.getRole().getPermissions())
			.flatMap(Set::stream)
			.collect(Collectors.toSet());
	}

}
