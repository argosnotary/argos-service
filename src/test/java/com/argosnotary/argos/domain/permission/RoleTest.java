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
package com.argosnotary.argos.domain.permission;


import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.argosnotary.argos.domain.roles.Permission;
import com.argosnotary.argos.domain.roles.Role;
import com.argosnotary.argos.domain.roles.Role.Contributor;
import com.argosnotary.argos.domain.roles.Role.LinkAdder;
import com.argosnotary.argos.domain.roles.Role.Owner;
import com.argosnotary.argos.domain.roles.Role.Reader;
import com.argosnotary.argos.domain.roles.Role.Releaser;

class RoleTest {
    Set<Permission> permissions;

    @BeforeEach
    void setUp() throws Exception {
        permissions = new HashSet<>();
        permissions.addAll(Set.of( 
                Permission.ROLE_WRITE,
                Permission.WRITE,
                Permission.READ, 
                Permission.LINK_ADD));
        
    }

    @Test
    void permissionTest() {
    	Role reader = new Reader();
    	Role contributor = new Contributor();
    	Role owner = new Owner();
    	Role linkAdder = new LinkAdder();
    	Role releaser = new Releaser();
        assertThat(reader.getPermissions(), is(Set.of(Permission.READ)));
        assertThat(contributor.getPermissions(), is(Set.of(Permission.READ,Permission.LINK_ADD,Permission.RELEASE,Permission.WRITE)));
        assertThat(owner.getPermissions(), is(Set.of(Permission.READ,Permission.LINK_ADD,Permission.RELEASE,Permission.WRITE,Permission.ROLE_WRITE)));
        assertThat(linkAdder.getPermissions(), is(Set.of(Permission.READ, Permission.LINK_ADD)));
        assertThat(releaser.getPermissions(), is(Set.of(Permission.READ, Permission.RELEASE)));
    }

}
