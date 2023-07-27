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
package com.argosnotary.argos.service.mongodb.nodes;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.argosnotary.argos.domain.nodes.Node;

public interface NodeRepository extends MongoRepository<Node, UUID> {
	
	public List<Node> findByPathToRoot(UUID resourceId);
	
	@Query("{_id: {$in: ?0}}")
	public List<Node> findWithIds(Set<UUID> ids);
	
	@Query("{ 'pathToRoot' : {$in: ?0}}")
	public List<Node> findWithResourceIdsUpTree(Set<UUID> resourceIds);
	
	@Query("{ 'pathToRoot' : {$in: ?1}, '_class': ?0}")
	public List<Node> findWithClassAndResourceIdsUpTree(String clazz, Set<UUID> resourceIds);
	
	@Query("{ '_id' : {$in: ?1}, '_class': ?0 }")
	public List<Node> findWithClassAndResourceIds(String clazz, Set<UUID> resourceIds);
	
	@Query("{ '_id' : {$in: ?0} }")
	public List<Node> findWithResourceIds(List<UUID> resourceIds);
	
	@Query(value="{ '_id' : ?1,  '_class': ?0 }", exists=true)
	public boolean existsByClassAndId(String clazz, UUID id);
	
	@Query(value="{ 'name' : ?1,  '_class': ?0 }", exists=true)
	public boolean existsByClassAndName(String clazz, String name);
	
	public Boolean existsByParentIdAndName(UUID parentId, String name);

	
}
