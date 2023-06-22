package com.argosnotary.argos.service.mongodb.roles;

import java.util.List;
import java.util.UUID;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.argosnotary.argos.domain.roles.RoleAssignment;

public interface RoleAssignmentRepository extends MongoRepository<RoleAssignment, UUID> {
	
	public void deleteByResourceId(UUID resourceId);
	
	public void deleteByIdentityId(UUID identityId);
	
	public List<RoleAssignment> findByResourceId(UUID resourceId);
	
	public List<RoleAssignment> findByIdentityId(UUID identityId);
	
	@Query("{'identityId': ?1, 'resourceId' : {$in: ?0 }}")
	public List<RoleAssignment> findByResourceIdsAndIdentityId(List<UUID> resourceIds, UUID identityId);

}
