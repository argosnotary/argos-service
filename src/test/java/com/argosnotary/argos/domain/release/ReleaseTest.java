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
package com.argosnotary.argos.domain.release;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.Set;

import org.junit.jupiter.api.Test;

class ReleaseTest {

    protected static final String HASH = "71ed24f24e838b18a4bc53aac2638155692b43289ca9778c37139859fc6e619d";
    protected static final Set<String> ARTIFACT_LIST;    

    String documentId = "documentId";

    static {
        ARTIFACT_LIST = Set.of("string2", "string");
    }

    @Test
    void createHashFromArtifactList() {
        String result = Release.calculateReleasedProductsHashesHash(ARTIFACT_LIST);
        assertThat(result, is(HASH));
    }
    
    @Test
    void equalsTest() {
        Release release = Release.builder().releasedProductsHashesHash(HASH).releasedProductsHashes(ARTIFACT_LIST).build();
        Release release2 = Release.builder().releasedProductsHashesHash(HASH).releasedProductsHashes(ARTIFACT_LIST).build();
        assertThat(release, is(release2));
    }
}