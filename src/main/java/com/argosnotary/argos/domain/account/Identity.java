package com.argosnotary.argos.domain.account;

import java.util.UUID;

import org.springframework.data.annotation.Id;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public abstract class Identity {
	@Id
	private UUID id;
	@NotNull
    private String name;
    
    Identity(UUID id,
			String name) {
		this.id = id == null ? UUID.randomUUID() : id;
		this.name = name;
	}

}
