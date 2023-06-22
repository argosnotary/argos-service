/*
 * Argos Notary - A new way to secure the Software Supply Chain
 *
 * Copyright (C) 2019 - 2020 Rabobank Nederland
 * Copyright (C) 2019 - 2022 Gerard Borst <gerard.borst@argosnotary.com>
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
package com.argosnotary.argos.service.mongodb.release;

import static com.argosnotary.argos.service.mongodb.release.ReleaseRepositoryImpl.HASHES;
import static com.argosnotary.argos.service.mongodb.release.ReleaseRepositoryImpl.ID_FIELD;
import static com.argosnotary.argos.service.mongodb.release.ReleaseRepositoryImpl.RELEASE_ARTIFACTS_FIELD;
import static com.argosnotary.argos.service.mongodb.release.ReleaseRepositoryImpl.RELEASE_DATE_FIELD;
import static com.argosnotary.argos.service.mongodb.release.ReleaseRepositoryImpl.SUPPLY_CHAIN_PATH_FIELD;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.core.convert.converter.Converter;

import com.argosnotary.argos.domain.release.ReleaseDossierMetaData;
import com.argosnotary.argos.domain.release.ReleaseDossierMetaData.ReleaseDossierMetaDataBuilder;

public class DocumentToReleaseDossierMetaDataConverter implements Converter<Document, ReleaseDossierMetaData>{

    @Override
    public ReleaseDossierMetaData convert(Document source) {
        List<Document> releaseArtifactsList = source
                .getList(RELEASE_ARTIFACTS_FIELD, Document.class,
                        Collections.emptyList());
        ReleaseDossierMetaDataBuilder builder =  ReleaseDossierMetaData.builder()                
                .releaseArtifacts(convertToReleaseArtifacts(releaseArtifactsList))
                .releaseDate(new OffsetDateTimeReadConverter().convert(source.getDate(RELEASE_DATE_FIELD)))
                .supplyChainPath(source.getString(SUPPLY_CHAIN_PATH_FIELD));
        
        if (source.containsKey(ID_FIELD)) {
            builder.documentId(((ObjectId)source.get(ID_FIELD))
                    .toHexString());
        }
        return builder.build();
    }

    private static List<List<String>> convertToReleaseArtifacts(List<Document> releaseArtifacts) {
        return releaseArtifacts.stream()
                .map(d -> (List<String>) d.get(HASHES))
                .collect(Collectors.toList());
    }

}
