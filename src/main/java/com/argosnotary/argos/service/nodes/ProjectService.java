package com.argosnotary.argos.service.nodes;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.argosnotary.argos.domain.nodes.Project;

public interface ProjectService {
	
	/**
	 * Find projects up tree of resourceIds or if empty all authorized projects.
	 * @param resourceIds
	 * @return
	 */
	List<Project> find(Set<UUID> resourceIds);
	
	Project create(Project project);
	
	void delete(UUID projectId);
	
	Optional<Project> findById(UUID projectId);

}
