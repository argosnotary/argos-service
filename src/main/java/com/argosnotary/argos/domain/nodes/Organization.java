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
public class Organization implements Node {
	@Id
	private UUID id = UUID.randomUUID();
    @NotNull
	private String name;
	private List<UUID> pathToRoot = new ArrayList<>();

	@NotNull
	private Domain domain;
	
	@Transient
    @EqualsAndHashCode.Exclude
    private Node parent = null;
    
    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Set<Node> children = new HashSet<>();
    
    @Override
    public Optional<Node> getParent() {
    	return Optional.empty();
    }
    
    @Override
    public UUID getParentId() {
    	return null;
    }
    
    public Organization(UUID id, @NotNull String name, Domain domain) {
		super();
		if (id != null) {
			this.id = id;
		}
		this.name = name;
		this.pathToRoot = List.of(this.id);
		this.domain = domain;
	}

	@Override
	public void setParent(Node node) {
		if (node.getClass() == this.getClass()) {
			
		}
	}
}
