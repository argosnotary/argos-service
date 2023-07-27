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
	public Set<Project> find(Node node) {
		return nodeService.find(Project.class.getCanonicalName(), Optional.of(node))
				.stream().map(n -> (Project) n).collect(Collectors.toSet());
	}

	@Override
	public boolean exists(UUID projectId) {
		return nodeService.exists(Project.class, projectId);
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
