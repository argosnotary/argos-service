package com.argosnotary.argos.service.nodes;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.argosnotary.argos.domain.nodes.Node;
import com.argosnotary.argos.domain.nodes.Project;

public interface ProjectService {
	
	/**
	 * Find projects up tree of resourceIds or if empty all authorized projects.
	 * @param resourceIds
	 * @return
	 */
	Set<Project> find(Node node);
	
	Project create(Project project);
	
	void delete(UUID projectId);
	
	Optional<Project> findById(UUID projectId);

}
