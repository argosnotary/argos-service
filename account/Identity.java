package com.argosnotary.argos.domain.account;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.argosnotary.argos.domain.permission.RoleAssignment;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
public class Identity {
	private UUID id;
    private String name;
    private Map<UUID, Set<RoleAssignment>> roleAssignments;
    
    Identity(UUID id,
			String name, 
		    Set<RoleAssignment> theRoleAssignments) {
		this.id = id == null ? UUID.randomUUID() : id;
		this.name = name;
		this.roleAssignments = new HashMap<>();
		if (theRoleAssignments != null) {
			for (RoleAssignment as : theRoleAssignments) {
				Set<RoleAssignment> ras = this.roleAssignments.putIfAbsent(as.getResourceId(), new HashSet<>());
				ras.add(as);
			}
		}
	}
    
    public Optional<Set<RoleAssignment>> getRoleAssignmentsById(UUID resourceId) {
    	if (roleAssignments.containsKey(resourceId)) {
    		return Optional.of(roleAssignments.get(resourceId));
    	}
    	return	Optional.empty();
    }

}
