package com.argosnotary.argos.service.nodes;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.argosnotary.argos.domain.nodes.Organization;

public interface OrganizationService {
	
	/**
	 * Find all organizations where the account is authorized for
	 * @return
	 */
	List<Organization> find(Set<UUID> resourceIds);
	
	/**
	 * 
	 * @param organizationId
	 * @return
	 */
	Optional<Organization> findById(UUID organizationId);
	
	/**
	 * Create an Organization
	 * @param organization
	 * @param account This account will be the owner
	 * @return
	 */
	Organization create(Organization organization);
	
	void delete(UUID organizationId);
}
