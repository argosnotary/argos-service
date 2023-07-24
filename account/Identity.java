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
