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
package com.argosnotary.argos.domain.roles;

import java.util.Set;

import lombok.Data;

@Data
public class Role {

	private Set<Permission> permissions;

	public static class Owner extends Role {
		public Owner() {
			this.setPermissions(Set.of(Permission.ROLE_WRITE, Permission.READ, Permission.WRITE, Permission.LINK_ADD,
					Permission.RELEASE));
		}
	}

	public static class Contributor extends Role {
		public Contributor() {
			this.setPermissions(Set.of(Permission.READ, Permission.WRITE, Permission.LINK_ADD, Permission.RELEASE));
		}
	}

	public static class Reader extends Role {
		public Reader() {
			this.setPermissions(Set.of(Permission.READ));
		}
	}

	public static class LinkAdder extends Role {
		public LinkAdder() {
			this.setPermissions(Set.of(Permission.READ, Permission.LINK_ADD));
		}
	}

	public static class Releaser extends Role {
		public Releaser() {
			this.setPermissions(Set.of(Permission.READ, Permission.RELEASE));
		}
	}
}
