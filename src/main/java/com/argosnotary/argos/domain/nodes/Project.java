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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;

import com.argosnotary.argos.domain.ArgosError;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Project implements Node {
	@Id
    private UUID id = UUID.randomUUID();
    @NotNull
	private String name;
    private List<UUID> pathToRoot = new ArrayList<>();
    private UUID parentId;
    
    @Transient
    @EqualsAndHashCode.Exclude
    private Node parent;
    
    @Transient
    @EqualsAndHashCode.Exclude
    private Set<Node> children = new HashSet<>();

	public Project(@NotNull UUID id, @NotNull String name, @NotNull Node parent) {
		this.id = id;
		this.name = name;
		this.setParent(parent);
		this.parentId = parent.getId();
		this.parent.getChildren().add(this);
		this.pathToRoot.add(this.id);
		this.pathToRoot.addAll(parent.getPathToRoot());
	}
	
	@Override
	public void setParent(Node node) {
		if (!(node instanceof Organization || node instanceof ManagementNode)) {
			throw new ArgosError("Parent node of Project can only be a Organization or a ManagementNode but has "+node.getClass().toString());
		}
		this.parent = node;
		this.parentId = node.getId();
	}
    
	@Override
	public Optional<Node> getParent() {
		return Optional.of(this.parent);
	}

}
