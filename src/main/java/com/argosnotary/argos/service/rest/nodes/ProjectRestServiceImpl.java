package com.argosnotary.argos.service.rest.nodes;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.argosnotary.argos.domain.nodes.ManagementNode;
import com.argosnotary.argos.domain.nodes.Node;
import com.argosnotary.argos.domain.nodes.Organization;
import com.argosnotary.argos.domain.nodes.Project;
import com.argosnotary.argos.domain.roles.Permission;
import com.argosnotary.argos.service.auditlog.AuditLog;
import com.argosnotary.argos.service.nodes.NodeService;
import com.argosnotary.argos.service.nodes.ProjectService;
import com.argosnotary.argos.service.openapi.rest.model.RestProject;
import com.argosnotary.argos.service.roles.PermissionCheck;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ProjectRestServiceImpl implements ProjectRestService {
	
	private final ProjectService projectService;
	
	private final NodeService nodeService;
	
	private final ProjectMapper projectMapper;

	@Override
    @PermissionCheck(permissions = Permission.WRITE)
    @AuditLog
	public ResponseEntity<RestProject> createProject(UUID parentId, @Valid RestProject restProject) {

		Optional<Node> parent = nodeService.findById(parentId);
		if (!(parent.isPresent() 
				&& parent.get().getId().equals(restProject.getParentId()) 
				&& (parent.get() instanceof Organization || (parent.get() instanceof ManagementNode)))) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid parent"); 
		}
		
		if (nodeService.existsByParentIdAndName(restProject.getParentId(), restProject.getName())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
					String.format("Project with name [%s] already exists on parent [%s]", restProject.getName(), restProject.getParentId()));
		}
		
		Project node = projectService
				.create(projectMapper.convertFromRestProject(restProject));

        URI location = ServletUriComponentsBuilder
        		.fromPath("/projects")
                .path("/{projectId}")
                .buildAndExpand(node.getId())
                .toUri();
		return ResponseEntity.created(location).body(projectMapper.convertToRestProject(node));
	}

	@Override
    @PermissionCheck(permissions = Permission.WRITE)
    @Transactional
    @AuditLog
	public ResponseEntity<Void> deleteProjectById(UUID projectId) {
		projectService.findById(projectId).orElseThrow(this::projectNotFound);
		projectService.delete(projectId);
		return ResponseEntity.noContent().build();
	}

	@Override
    @PermissionCheck(permissions = Permission.READ)
	public ResponseEntity<RestProject> getProject(UUID projectId) {
		Project node = projectService.findById(projectId).orElseThrow(this::projectNotFound);
		return ResponseEntity.ok(projectMapper.convertToRestProject(node));
	}

	@Override
    @PermissionCheck(permissions = Permission.READ)
	public ResponseEntity<List<RestProject>> getProjects(UUID ancestorId) {
		Optional<Node> optNode = nodeService.findById(ancestorId);
		if (optNode.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Node with id [%s] not found", ancestorId));
		}
		return ResponseEntity.ok(projectService.find(optNode.get())
				.stream().map(projectMapper::convertToRestProject).collect(Collectors.toList()));
	}
	
	private ResponseStatusException projectNotFound() {
		return new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found");
	}
}
