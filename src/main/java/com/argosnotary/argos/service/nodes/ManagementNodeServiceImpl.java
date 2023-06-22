package com.argosnotary.argos.service.nodes;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.argosnotary.argos.domain.nodes.ManagementNode;
import com.argosnotary.argos.domain.nodes.Node;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ManagementNodeServiceImpl implements ManagementNodeService {
	
	private final NodeService nodeService;

	@Override
	public List<ManagementNode> find(Set<UUID> resourceIds) {
		return nodeService.find(ManagementNode.class.getCanonicalName(), resourceIds)
				.stream().map(n -> (ManagementNode) n).collect(Collectors.toList());
	}

	@Override
	public Optional<ManagementNode> findById(UUID managementNodeId) {
		Optional<Node> node = nodeService.findById(managementNodeId);
		if (node.isPresent() && (node.get() instanceof ManagementNode)) {
			return Optional.of((ManagementNode) node.get());
		}
		return Optional.empty();
	}

	@Override
	public ManagementNode create(ManagementNode managementNode) {
		return (ManagementNode) nodeService.create(managementNode);
	}

	@Override
	public void delete(UUID managementNodeId) {
		nodeService.delete(managementNodeId);
	}

}
