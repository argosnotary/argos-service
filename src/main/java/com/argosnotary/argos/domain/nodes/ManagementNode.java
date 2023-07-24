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

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
public class ManagementNode implements Node {
	@Id
	private UUID id = UUID.randomUUID();
    @NotNull
	private String name;
    private List<UUID> pathToRoot = new ArrayList<>();
    private UUID parentId;
    
    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Set<Node> children = new HashSet<>();
    @Transient
    @EqualsAndHashCode.Exclude
	private Node parent;

	public ManagementNode(@NotNull UUID id, @NotNull String name, @NotNull Node parent) {
		this.id = id;
		this.name = name;
		this.parentId = parent.getId();
		this.parent = parent;
		this.parent.getChildren().add(this);
		this.pathToRoot.add(this.id);
		this.pathToRoot.addAll(parent.getPathToRoot());
	}    
    
	@Override
	public Optional<Node> getParent() {
		return Optional.of(this.parent);
	}

}
