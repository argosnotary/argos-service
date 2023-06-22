package com.argosnotary.argos.service.roles;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.argosnotary.argos.domain.nodes.Node;
import com.argosnotary.argos.domain.roles.Permission;
import com.argosnotary.argos.domain.roles.Role;
import com.argosnotary.argos.domain.roles.RoleAssignment;
import com.argosnotary.argos.service.mongodb.roles.RoleAssignmentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoleAssignmentServiceImpl implements RoleAssignmentService {
	
	private final RoleAssignmentRepository roleAssignmentRepository;

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
	public Set<Permission> findByNodeAndIdentityId(Node node, UUID identityId) {
		List<UUID> resourceIds = node.getPathToRoot();
		return roleAssignmentRepository.findByResourceIdsAndIdentityId(resourceIds, identityId)
			.stream()
			.map(ras -> ras.getRole().getPermissions())
			.flatMap(Set::stream)
			.collect(Collectors.toSet());
	}

	@Override
	public List<RoleAssignment> findByIdentityId(UUID accountId) {
		return roleAssignmentRepository.findByIdentityId(accountId);
	}

}
