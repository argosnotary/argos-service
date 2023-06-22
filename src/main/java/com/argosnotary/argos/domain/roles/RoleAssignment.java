package com.argosnotary.argos.domain.roles;

import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@Document(collection="roleassignments")
public class RoleAssignment {
	@Id
	@Builder.Default
	private UUID id = UUID.randomUUID();
	@NotNull
	private Role role;
	@NotNull
	@Indexed
	private UUID resourceId;
	@NotNull
	@Indexed
	private UUID identityId;
}
