package com.argosnotary.argos.service.nodes;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.argosnotary.argos.domain.nodes.Node;
import com.argosnotary.argos.domain.nodes.Project;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {
	
	private final NodeService nodeService;

	@Override
	public List<Project> find(Set<UUID> resourceIds) {
		return nodeService.find(Project.class.getCanonicalName(), resourceIds)
				.stream().map(n -> (Project) n).collect(Collectors.toList());
	}

	@Override
	public Project create(Project project) {
		return (Project) nodeService.create(project);
	}

	@Override
	public void delete(UUID projectId) {
		nodeService.delete(projectId);
	}

	@Override
	public Optional<Project> findById(UUID projectId) {
		Optional<Node> node = nodeService.findById(projectId);
		if (node.isEmpty() || !(node.get() instanceof Project)) {
			return Optional.empty();
		}
		return Optional.of((Project) node.get());
	}

}
