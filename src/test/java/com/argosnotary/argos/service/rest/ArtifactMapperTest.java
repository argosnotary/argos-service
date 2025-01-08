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
package com.argosnotary.argos.service.rest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import com.argosnotary.argos.domain.link.Artifact;
import com.argosnotary.argos.service.openapi.rest.model.RestArtifact;

class ArtifactMapperTest {

    public static final String HASH = "hash";
    public static final String URI = "uri";
    private ArtifactMapper artifactMapper;

    @BeforeEach
    public void setup() {
        artifactMapper = Mappers.getMapper(ArtifactMapper.class);
    }

    @Test
    void mapToArtifacts() {
    	Artifact artifact = new Artifact(HASH, URI);
    	Artifact a = artifactMapper.restArtifactToArtifact(artifactMapper.artifactToRestArtifact(artifact));
    	assertEquals(artifact, a);
    	
    	RestArtifact ra = artifactMapper.artifactToRestArtifact(artifact);
    	

    	a = artifactMapper.restArtifactToArtifact(artifactMapper.artifactToRestArtifact(null));
    	assertNull(a);
    	
    	List<Artifact> as = artifactMapper.restArtifactListToArtifactList(List.of(ra));
    	assertThat(as.size(), is(1));
    	assertThat(as, contains(artifact));
    	
    	List<RestArtifact> ras = artifactMapper.artifactListToRestArtifactList(List.of(artifact));
    	assertThat(as.size(), is(1));
    	assertThat(as, contains(artifact));
    	
    	as = artifactMapper.restArtifactListToArtifactList(List.of());
    	assertTrue(as.isEmpty());
    	
    	ras = artifactMapper.artifactListToRestArtifactList(List.of());
    	assertTrue(ras.isEmpty());
    	
    	List<Set<Artifact>> lsa = artifactMapper.mapToArtifacts(List.of(List.of(ra)));
    	assertEquals(artifact, lsa.get(0).iterator().next());
    	
    	lsa = artifactMapper.mapToArtifacts(List.of());
    	assertTrue(lsa.isEmpty());
    	
    	Set<Artifact> sa = artifactMapper.mapToSetArtifacts(List.of(ra));
        assertThat(sa, hasSize(1));
    	assertEquals(artifact, sa.iterator().next());
    	
    	sa = artifactMapper.mapToSetArtifacts(List.of());
    	assertTrue(sa.isEmpty());
    }

}