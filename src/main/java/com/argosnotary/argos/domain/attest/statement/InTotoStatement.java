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
package com.argosnotary.argos.domain.attest.statement;

import java.net.URL;
import java.util.Collections;
import java.util.List;

import org.springframework.data.annotation.Transient;

import com.argosnotary.argos.domain.ArgosError;
import com.argosnotary.argos.domain.attest.Predicate;
import com.argosnotary.argos.domain.attest.ResourceDescriptor;
import com.argosnotary.argos.domain.attest.Statement;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode(callSuper=true)
@ToString(callSuper=true)
public class InTotoStatement extends Statement {
	@JsonIgnore
	@Transient
	public static final String TYPE = "https://in-toto.io/Statement/v1";
	
	private final List<ResourceDescriptor> subject;

	@Transient
	private final URL predicateType;
	
	private final Predicate predicate;

	public InTotoStatement(List<ResourceDescriptor> subject, Predicate predicate) {
		super(TYPE);
		if (subject == null || predicate == null) {
			throw new ArgosError(String.format("Wrong InTotoStatement defintion, properties are null: subject: [%s], predicate: [%s]", subject == null ? null : subject.toString(), predicate == null ? null : predicate.toString()));
		}
		this.subject =  Collections.unmodifiableList(subject);
		this.predicateType = predicate.getPredicateType();
		this.predicate = predicate;
	}	

	@Override
	public Statement cloneCanonical() {
		return new InTotoStatement(subject.stream().sorted().map(ResourceDescriptor::cloneCanonical).toList(), predicate.cloneCanonical());
	}
	
}
