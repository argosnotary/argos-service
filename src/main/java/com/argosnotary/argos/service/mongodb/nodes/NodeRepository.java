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
	
//	@Aggregation(pipeline = {
//			"{$match: {_id: {$in: ?0 }}}",
//		    "{$group: {_id: null, pathIds: {$push: '$pathToRoot'}}}",
//		    "{$project: {'result': {$reduce:{input: '$pathIds',initialValue: [],in:{ $concatArrays: [ '$$value', '$$this' ] }}}, _id: 0}}",
//		    "{$lookup:{from: 'nodes', localField: 'result', foreignField: '_id', as: 'children'}}",
//		    "{$unset: 'result'}"
//    })
//	public NodeList findClassInTree(Set<UUID> resourceIds);
	
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
