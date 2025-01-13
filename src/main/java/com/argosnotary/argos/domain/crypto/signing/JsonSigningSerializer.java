/*
 * Argos Notary - A new way to secure the Software Supply Chain
 *
 * Copyright (C) 2019 - 2020 Rabobank Nederland
 * Copyright (C) 2019 - 2025 Gerard Borst <gerard.borst@argosnotary.com>
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
package com.argosnotary.argos.domain.crypto.signing;

import static java.util.Comparator.comparing;

import org.mapstruct.factory.Mappers;

import com.argosnotary.argos.domain.ArgosError;
import com.argosnotary.argos.domain.layout.Layout;
import com.argosnotary.argos.domain.layout.Step;
import com.argosnotary.argos.domain.link.Artifact;
import com.argosnotary.argos.domain.link.Link;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class JsonSigningSerializer implements SigningSerializer {
	
	private static final JsonMapper jsonMapper = JsonMapper.builder()
			.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true)
    		.addModule(new JavaTimeModule())
			.serializationInclusion(JsonInclude.Include.NON_NULL)
			.build();

    @Override
    public String serialize(Link link) {
        Link linkClone = Mappers.getMapper(Cloner.class).clone(link);
        linkClone.getMaterials().sort(comparing(Artifact::getUri));
        linkClone.getProducts().sort(comparing(Artifact::getUri));
        return serializeSignable(linkClone);
    }

    @Override
    public String serialize(Layout layout) {
        Layout layoutClone = Mappers.getMapper(Cloner.class).clone(layout);
        layoutClone.getSteps().sort(comparing(Step::getName));
        return serializeSignable(layoutClone);
    }

	@Override
	public <T> String serialize(Canonicalable<T> signable) {
		T clone = signable.cloneCanonical();
		return serializeSignable(clone);
	}

    private String serializeSignable(Object signable) {
        try {
        	return jsonMapper.writeValueAsString(signable);
        } catch (JsonProcessingException e) {
            throw new ArgosError(e.getMessage(), e);
        }
    }

}
