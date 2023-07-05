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

//	@Override
//	public Set<RoleAssignment> findByResourceIdsAndAccountId(List<UUID> resourceIds, UUID accountId) {
//		return roleAssignmentRepository.findByIdentityIdAnd(accountId).stream()
//				.filter(ra -> resourceIds.contains(ra.getResourceId()))
//				.collect(Collectors.toSet());
//	}

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

		if (optAccount.get() instanceof ServiceAccount) {
			return ((ServiceAccount)optAccount.get()).getRoleAssignments().stream()
					.filter(ra -> pathResourceIds.contains(ra.getResourceId()))
					.map(rs -> rs.getRole().getPermissions())
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
