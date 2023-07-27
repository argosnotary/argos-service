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
package com.argosnotary.argos.domain.nodes;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.NotNull;


@Document(collection="nodes")
@CompoundIndex(name = "parentId_name", def = "{'parentId' : 1, 'name': 1}", unique=true)
public interface Node {

	public default void visit(NodeVisitor<?> treeNodeVisitor) {
		treeNodeVisitor.visitEnter(this);
		getChildren().forEach(child -> child.visit(treeNodeVisitor));
		treeNodeVisitor.visitExit(this);
	}
    
    public default void visitDown(NodeVisitor<?> treeNodeVisitor) {
        if (isRoot()) {
            treeNodeVisitor.visitEndPoint(this);
        } else {
        	treeNodeVisitor.visitEnter(this);
        	this.getParent().ifPresent(p -> p.visitDown(treeNodeVisitor));
        	treeNodeVisitor.visitExit(this);
        }
    }
    
    @Id
    public UUID getId();
    
    public String getName();
    
    public void setId(UUID id);
    
    public UUID getParentId();
    
    @Transient
    public Optional<Node> getParent();
    
    @Transient
    public void setParent(@NotNull Node node);
    
    @Transient
    public Set<Node> getChildren();
    
    public List<UUID> getPathToRoot();
    
    public void setPathToRoot(List<UUID> path);
    
    public default boolean isLeaf() {
		return this.getChildren().isEmpty();
	}

	public default boolean isRoot() {
		return this.getParent().isEmpty();
	}
    
}
