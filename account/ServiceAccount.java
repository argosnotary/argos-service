/*
 * Argos Notary - A new way to secure the Software Supply Chain
 *
 * Copyright (C) 2019 - 2020 Rabobank Nederland
 * Copyright (C) 2019 - 2022 Gerard Borst <gerard.borst@argosnotary.com>
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

import com.argosnotary.argos.domain.crypto.KeyPair;
import com.argosnotary.argos.domain.permission.Role;
import com.argosnotary.argos.domain.permission.RoleAssignment;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class ServiceAccount extends Account implements TreeNode {

	public static final String SA_PROVIDER_NAME = "saprovider";
	
    private UUID projectId;
    
    private static final Role releaser;
    private static final Role linkAdder;
    static {
    	releaser = new Role.Releaser();
    	linkAdder = new Role.LinkAdder();
    }

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
                inactiveKeyPairs == null ? new HashSet<>() : inactiveKeyPairs,
                Set.of(
                		RoleAssignment.builder().resourceId(projectId).role(releaser).build(), 
                		RoleAssignment.builder().resourceId(projectId).role(linkAdder).build()));
        this.projectId = projectId;
    }

	@Override
	public boolean isLeafNode() {
		return true;
	}

	@Override
	public UUID getResourceId() {
		return this.getId();
	}

	@Override
	public Optional<UUID> getParentId() {
		return Optional.of(this.getProjectId());
	}

	@Override
	public List<TreeNode> getChildren() {
		return null;
	}
}
