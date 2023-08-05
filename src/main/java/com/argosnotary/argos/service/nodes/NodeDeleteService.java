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

import org.springframework.stereotype.Service;

import com.argosnotary.argos.domain.nodes.Node;
import com.argosnotary.argos.domain.nodes.Project;
import com.argosnotary.argos.domain.nodes.SupplyChain;
import com.argosnotary.argos.domain.nodes.TreeNode;
import com.argosnotary.argos.domain.nodes.TreeNodeVisitor;
import com.argosnotary.argos.service.mongodb.account.ServiceAccountRepository;
import com.argosnotary.argos.service.mongodb.layout.ApprovalConfigurationRepository;
import com.argosnotary.argos.service.mongodb.layout.LayoutMetaBlockRepository;
import com.argosnotary.argos.service.mongodb.layout.ReleaseConfigurationRepository;
import com.argosnotary.argos.service.mongodb.link.LinkMetaBlockRepository;
import com.argosnotary.argos.service.mongodb.nodes.NodeRepository;
import com.argosnotary.argos.service.roles.RoleAssignmentService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NodeDeleteService implements TreeNodeVisitor<Optional<TreeNode>>{
	
    private final NodeRepository nodeRepository;
    private final LayoutMetaBlockRepository layoutMetaBlockRepository;
    private final LinkMetaBlockRepository linkMetaBlockRepository;
    private final ApprovalConfigurationRepository approvalConfigurationRepository;
    private final ServiceAccountRepository serviceAccountRepository;
    private final RoleAssignmentService roleAssignmentService;
    private final ReleaseConfigurationRepository releaseConfigurationRepository;

    public void deleteNode(TreeNode node) {
    	node.visit(this);
    }

    @Override
    public void visitEnter(TreeNode node) {
		if (node.getNode() instanceof SupplyChain supplyChainNode) {
			deleteSupplyChain(supplyChainNode);
		}
		if (node.getNode() instanceof Project) {
			serviceAccountRepository.deleteByProjectId(node.getNode().getId());
		}
    }

    @Override
    public void visitExit(TreeNode node) {
    	removeRoleAssignments(node.getNode());
    	nodeRepository.deleteById(node.getNode().getId());
    }

	@Override
	public void visitEndPoint(TreeNode node) {
		throw new UnsupportedOperationException("visitEndPoint method not implemented");
		
	}

    private void deleteSupplyChain(SupplyChain supplyChain) {
        layoutMetaBlockRepository.deleteBySupplyChainId(supplyChain.getId());
        linkMetaBlockRepository.deleteBySupplyChainId(supplyChain.getId());
        approvalConfigurationRepository.deleteBySupplyChainId(supplyChain.getId());
        releaseConfigurationRepository.deleteBySupplyChainId(supplyChain.getId());
    }
	
	private void removeRoleAssignments(Node node) {
		roleAssignmentService.deleteByResourceId(node.getId());
	}
}
