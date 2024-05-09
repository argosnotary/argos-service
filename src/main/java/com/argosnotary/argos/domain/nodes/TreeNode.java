/*
 * Argos Notary - A new way to secure the Software Supply Chain
 *
 * Copyright (C) 2019 - 2020 Rabobank Nederland
 * Copyright (C) 2019 - 2024 Gerard Borst <gerard.borst@argosnotary.com>
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
package com.argosnotary.argos.domain.nodes;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.argosnotary.argos.domain.ArgosError;

import jakarta.validation.ValidationException;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
public class TreeNode {
	Node node;
	@EqualsAndHashCode.Exclude
    @ToString.Exclude
	Set<TreeNode> children = new HashSet<>();
	Optional<TreeNode> parent;
	
	public TreeNode(@NotNull Node node,  @NotNull Optional<TreeNode> parent) {
		this.node = node;
		if ((parent.isPresent() && (node.getParentId() == null || (!node.getParentId().equals(parent.get().getNode().getId())))) 
				|| (parent.isEmpty() && node.getParentId() != null)) {
			throw new ValidationException(String.format("Inconsistent creation of Treenode with node [%s] and parent [%s]", node.toString(), parent.isEmpty() ? "empty": parent.get().toString()));
		}
		this.node = node;
		this.parent = parent;
		if (parent.isPresent()) {
			parent.get().getChildren().add(this);
		}
	}

	public void visit(TreeNodeVisitor<?> treeNodeVisitor) {
		treeNodeVisitor.visitEnter(this);
		getChildren().forEach(child -> child.visit(treeNodeVisitor));
		treeNodeVisitor.visitExit(this);
	}
    
    public void visitDown(TreeNodeVisitor<?> treeNodeVisitor) {
        if (isRoot()) {
            treeNodeVisitor.visitEndPoint(this);
        } else {
        	treeNodeVisitor.visitEnter(this);
        	this.getParent().ifPresent(p -> p.visitDown(treeNodeVisitor));
        	treeNodeVisitor.visitExit(this);
        }
    }
    
    public boolean isLeaf() {
		return this.getChildren().isEmpty();
	}

	public boolean isRoot() {
		return this.getParent().isEmpty();
	}
	
	public static TreeNode createUpTree(Set<Node> nodes) {
		TreeNode root = null;
		Map<UUID, Node> nodeMap = nodes.stream().collect(Collectors.toMap(Node::getId, Function.identity()));
		for (Node node : nodes) {
			if (!nodeMap.containsKey(node.getParentId())) {
				if (root == null) {
					node.setParentId(null);
					root = new TreeNode(node, Optional.empty());
				} else {
					throw new ArgosError("More than 1 pivot");
				}
			}
		}
		if (root == null) {
			throw new ArgosError("No pivot");
		}
		Map<UUID, Set<Node>> parentMap = new HashMap<>();
		for (Node node : nodes) {
			if (node.getParentId() == null) {
				continue;
			}
			if (!parentMap.containsKey(node.getParentId())) {
				parentMap.put(node.getParentId(), new HashSet<>());
			}
			parentMap.get(node.getParentId()).add(node);
			
		}
		return createUpTree(root, parentMap);
	}
	
	private static TreeNode createUpTree(TreeNode treeNode, Map<UUID, Set<Node>> parentMap) {
		if (!parentMap.containsKey(treeNode.getNode().getId())) {
			return treeNode;
		} else {
			for (Node node : parentMap.get(treeNode.getNode().getId())) {
				treeNode.getChildren().add(createUpTree(new TreeNode(node, Optional.of(treeNode)), parentMap));
			}
			return treeNode;
		}
	}
	
	public static TreeNode getPathToRoot(Node leaf, Set<Node> nodes) {
		Map<UUID, Node> nodeMap = nodes.stream().collect(Collectors.toMap(Node::getId, Function.identity()));
		return getPathToRoot(leaf, nodeMap);
	}
	
	private static TreeNode getPathToRoot(Node leaf, Map<UUID, Node> nodeMap) {
		if (leaf.getParentId() == null) {
			return new TreeNode(leaf, Optional.empty());
		}
		return new TreeNode(leaf, Optional.of(getPathToRoot(nodeMap.get(leaf.getParentId()), nodeMap)));
		
	}

}
