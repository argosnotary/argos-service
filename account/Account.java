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

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.argosnotary.argos.domain.crypto.KeyPair;
import com.argosnotary.argos.domain.permission.RoleAssignment;

@Getter
@Setter
@EqualsAndHashCode
public abstract class Account extends Identity implements Serializable {
	private String providerName;
    private String providerSubject;
    private KeyPair activeKeyPair;
    private Set<KeyPair> inactiveKeyPairs;

	Account(UUID id,
			String providerName,
		    String providerSubject, 
		    String name, 
		    KeyPair activeKeyPair, 
		    Set<KeyPair> inactiveKeyPairs,
		    Set<RoleAssignment> roleAssignments) {
		super(id, name, roleAssignments);
		this.providerName = providerName;
		this.providerSubject = providerSubject;
		this.activeKeyPair = activeKeyPair;
		this.inactiveKeyPairs = inactiveKeyPairs == null ? new HashSet<>() : inactiveKeyPairs;
	}
}
