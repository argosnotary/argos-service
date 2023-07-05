package com.argosnotary.argos.service.nodes;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.argosnotary.argos.domain.account.Account;
import com.argosnotary.argos.domain.account.PersonalAccount;
import com.argosnotary.argos.domain.nodes.Node;
import com.argosnotary.argos.domain.nodes.Organization;
import com.argosnotary.argos.domain.roles.Permission;
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
				.stream().map(n -> (Organization) n).collect(Collectors.toList());
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
		PersonalAccount account = (PersonalAccount)accountSecurityContext.getAuthenticatedAccount().get();
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
	public boolean existsById(UUID organizationId) {
		return nodeService.exists(Organization.class, organizationId);
	}

	@Override
	public boolean existsByName(String name) {
		// TODO Auto-generated method stub
		return nodeService.exists(Organization.class, name);
	}

}
