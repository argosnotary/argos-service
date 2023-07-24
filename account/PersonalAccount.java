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

import com.argosnotary.argos.domain.crypto.KeyPair;
import com.argosnotary.argos.domain.permission.LocalPermissions;
import com.argosnotary.argos.domain.permission.Role;
import com.argosnotary.argos.domain.permission.RoleAssignment;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode(callSuper=true)
public class PersonalAccount extends Account {
	@Builder
    public PersonalAccount(
            UUID id,
            String providerName,
            String providerSubject,
            String name,
            String fullName,
            String givenName,
            String familyName,
            String email,
            URL picture,
            KeyPair activeKeyPair,
            Set<KeyPair> inactiveKeyPairs,
            Set<Role> roles,
            Set<RoleAssignment> roleAssignments
    ) {
        super(id,
        		providerName,
                providerSubject,
                name,
                activeKeyPair,
                inactiveKeyPairs == null ? new HashSet<>() : inactiveKeyPairs,
                roleAssignments == null ? new HashSet<>() : roleAssignments);
        this.picture = picture;
        this.email = email;
        this.fullName = fullName;
        this.givenName = givenName;
        this.familyName = familyName;
    }

    private String fullName;
    private String givenName;
    private String familyName;
    private String email;
    private URL picture;

    public boolean isForOidcEqual(PersonalAccount b) {
    	return b != null
    			&& this.getName().equals(b.getName())
    			&& ((this.getGivenName() == null && b.getGivenName() == null) 
    					|| (this.getGivenName() != null && this.getGivenName().equals(b.getGivenName())))
    			&& ((this.getFamilyName() == null && b.getFamilyName() == null) 
    					|| (this.getFamilyName() != null && this.getFamilyName().equals(b.getFamilyName())))
    			&& ((this.getFullName() == null && b.getFullName() == null) 
    					|| (this.getFullName() != null && this.getFullName().equals(b.getFullName())))
    			&& this.getEmail().equals(b.getEmail())
    			&& (this.getPicture() == null && b.getPicture() == null
    			|| (this.getPicture() != null && this.getPicture().equals(b.getPicture())));
    }

    public void updateExistingWithOidc(PersonalAccount oidcAccount) {
    	this.setName(oidcAccount.getName());
    	this.setFullName(oidcAccount.getFullName());
    	this.setGivenName(oidcAccount.getGivenName());
    	this.setFamilyName(oidcAccount.getFamilyName());
    	this.setFullName(oidcAccount.getFullName());
    	this.setEmail(oidcAccount.getEmail());
    	this.setPicture(oidcAccount.getPicture());
    }

}
