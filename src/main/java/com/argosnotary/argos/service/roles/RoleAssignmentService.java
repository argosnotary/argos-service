package com.argosnotary.argos.service.roles;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.argosnotary.argos.domain.nodes.Node;
import com.argosnotary.argos.domain.roles.Permission;
import com.argosnotary.argos.domain.roles.Role;
import com.argosnotary.argos.domain.roles.RoleAssignment;

public interface RoleAssignmentService {
	
	public RoleAssignment save(RoleAssignment RoleAssignment);
	
	public RoleAssignment create(UUID resourceId, UUID accountId, Role role);
	
	public void delete(UUID roleAssignmentId);
	
	public void deleteByResourceId(UUID resourceId);
	
	public List<RoleAssignment> findByResourceId(UUID resourceId);
	
	public List<RoleAssignment> findByIdentity();
	
	public Set<Permission> findAllPermissionDownTree(Node node);
	

}
