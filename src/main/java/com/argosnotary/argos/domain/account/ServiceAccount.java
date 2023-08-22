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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import com.argosnotary.argos.domain.crypto.KeyPair;
import com.argosnotary.argos.domain.roles.Role;
import com.argosnotary.argos.domain.roles.RoleAssignment;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@Document(collection="serviceaccounts")
@CompoundIndex(name = "projectId_name", def = "{'projectId' : 1, 'name': 1}", unique=true)
public class ServiceAccount extends Account {

	public static final String SA_PROVIDER_NAME = "saprovider";
	
    private UUID projectId;
    
    static {
    	 Set<Role> rs = new HashSet<>();
    	 rs.add(new Role.Releaser());
    	 rs.add(new Role.LinkAdder());
    	 rs.add(new Role.AttestationAdder());
    	 roles = Collections.unmodifiableSet(rs);
    }
    
    public static final Set<Role> roles;

    @Builder
    public ServiceAccount(
    		UUID id,
            String providerSubject,
    		String name,
    		KeyPair activeKeyPair,
            Set<KeyPair> inactiveKeyPairs, 
            UUID projectId) {
        super(
        		id,
        		SA_PROVIDER_NAME,
        		providerSubject,
        		name,
        		activeKeyPair,
                inactiveKeyPairs);
        this.projectId = projectId;
    }
    
    public Set<RoleAssignment> getRoleAssignments() {
    	return roles.stream()
    			.map(r -> RoleAssignment.builder().identityId(getId()).resourceId(projectId).role(r).build())
    			.collect(Collectors.toSet());
    }
}
