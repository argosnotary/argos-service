package com.argosnotary.argos.service.nodes;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.argosnotary.argos.domain.nodes.Node;
import com.argosnotary.argos.domain.nodes.Organization;
import com.argosnotary.argos.domain.nodes.SupplyChain;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SupplyChainServiceImpl implements SupplyChainService {
	
	private final NodeService nodeService;

	@Override
	public Set<SupplyChain>  find(Node node) {
		return nodeService.find(SupplyChain.class.getCanonicalName(), Optional.of(node))
				.stream().map(n -> (SupplyChain) n).collect(Collectors.toSet());
	}

	@Override
	public SupplyChain create(SupplyChain supplyChain) {
		return (SupplyChain) nodeService.create(supplyChain);
	}

	@Override
	public void delete(UUID supplyChainId) {
		nodeService.delete(supplyChainId);
	}

	@Override
	public Optional<SupplyChain> findById(UUID supplyChainId) {
		Optional<Node> node = nodeService.findById(supplyChainId);
		if (node.isEmpty() || !(node.get() instanceof SupplyChain)) {
			return Optional.empty();
		}
		return Optional.of((SupplyChain) node.get());
	}

	@Override
	public boolean exists(UUID supplyChainId) {
		return nodeService.exists(SupplyChain.class, supplyChainId);
	}

	@Override
	public Optional<String> getQualifiedName(UUID supplyChainId) {
		Optional<Node> supplyChainNode = nodeService.findById(supplyChainId);
		if (supplyChainNode.isEmpty() || !(supplyChainNode.get() instanceof SupplyChain)) {
			return Optional.empty();
		}
		return nodeService.getQualifiedName(supplyChainId);
	}

	@Override
	public SupplyChain update(SupplyChain supplyChain) {
		return (SupplyChain) nodeService.update(supplyChain);
	}

	@Override
	public Optional<Organization> getOrganization(UUID supplyChainId) {
		return nodeService.findRootNodeInPath(supplyChainId)
				.filter(node -> node instanceof Organization)
				.map(n -> Optional.of((Organization)n))
				.orElse(Optional.empty());
	}

}
