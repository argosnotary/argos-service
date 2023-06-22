package com.argosnotary.argos.service.mongodb.nodes;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.argosnotary.argos.domain.nodes.Node;

public interface NodeRepository extends MongoRepository<Node, UUID> {
	
	@Query("{ 'pathToRoot' : ?0 }")
	public List<Node> findInPathToRoot(UUID resourceId);
	
	@Query("{_id: {$in: ?0}}")
	public List<Node> findWithIds(Set<UUID> ids);
	
	@Query("{ 'pathToRoot' : {$in: ?1}}")
	public List<Node> findWithResourceIdsUpTree(Set<UUID> resourceIds);
	
	@Query("{ 'pathToRoot' : {$in: ?1}, '_class': ?0}")
	public List<Node> findWithClassAndResourceIdsUpTree(String clazz, Set<UUID> resourceIds);
	
	@Query("{ '_id' : {$in: ?1}, '_class': ?0 }")
	public List<Node> findWithClassAndResourceIds(String clazz, Set<UUID> resourceIds);
	
	@Query("{ '_id' : {$in: ?0} }")
	public List<Node> findWithResourceIds(List<UUID> resourceIds);
	
	@Query("{ '_id' : ?1,  '_class': ?0 }")
	public boolean existsByClassAndId(String clazz, UUID id);

	
}
