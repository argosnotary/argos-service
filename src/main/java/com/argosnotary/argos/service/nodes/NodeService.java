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

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.argosnotary.argos.domain.nodes.Node;
import com.argosnotary.argos.domain.nodes.Organization;

public interface NodeService {
	
	public Node create(Node node);
	
	public Node update(Node node);
	
	void delete(UUID resourceId);
	
	public Optional<Node> findById(UUID nodeId);
	
	/**
	 * Find nodes with Class clazz and authorized resourceIds in path to root and to leafs. 
	 * If resourceIds is empty return all authorized nodes
	 * @param clazz
	 * @param resourceIds
	 * @return
	 */
	public Set<Node> find(String clazz, Optional<Node> node);
	
	Organization findOrganizationInPath(UUID resourceId);
	
	public boolean exists(Class<? extends Node> clazz, UUID resourceId);
	
	public boolean exists(Class<? extends Node> clazz, String name);
	
	public boolean existsByParentIdAndName(UUID parentId, String name);
	
	Optional<String> getQualifiedName(UUID resourceId);

}
