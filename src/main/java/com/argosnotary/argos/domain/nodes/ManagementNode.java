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
