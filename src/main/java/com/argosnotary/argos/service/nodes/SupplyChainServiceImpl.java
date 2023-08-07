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
import com.argosnotary.argos.domain.nodes.Organization;
import com.argosnotary.argos.domain.nodes.SupplyChain;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SupplyChainServiceImpl implements SupplyChainService {
	
	private final NodeService nodeService;

	@Override
	public Set<SupplyChain> find(Optional<Node> optNode) {
		return nodeService.find(SupplyChain.class.getCanonicalName(), optNode)
				.stream().map(SupplyChain.class::cast).collect(Collectors.toSet());
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
	public SupplyChain create(SupplyChain supplyChain) {
		return (SupplyChain) nodeService.create(supplyChain);
	}

	@Override
	public void delete(UUID supplyChainId) {
		nodeService.delete(supplyChainId);
	}

	@Override
	public SupplyChain update(SupplyChain supplyChain) {
		return (SupplyChain) nodeService.update(supplyChain);
	}

}
