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
package com.argosnotary.argos.service.nodes;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.argosnotary.argos.domain.nodes.Organization;

public interface OrganizationService {
	
	/**
	 * Find all organizations where the account is authorized for
	 * @return
	 */
	List<Organization> find();
	
	/**
	 * 
	 * @param organizationId
	 * @return
	 */
	Optional<Organization> findById(UUID organizationId);
	
	public boolean existsByName(String name);
	
	boolean exists(UUID organizationId);
	
	/**
	 * Create an Organization
	 * @param organization
	 * @param account This account will be the owner
	 * @return
	 */
	Organization create(Organization organization);
	
	void delete(UUID organizationId);
}
