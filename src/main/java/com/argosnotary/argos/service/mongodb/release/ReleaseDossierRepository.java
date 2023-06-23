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

import java.io.InputStream;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Component;

import com.argosnotary.argos.domain.release.ReleaseDossier;
import com.argosnotary.argos.domain.release.ReleaseDossierMetaData;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReleaseDossierRepository {

    public static final String ID_FIELD = "_id";
    public static final String METADATA_RELEASE_ARTIFACTS_ARTIFACTS_HASH_FIELD = "metadata.releaseArtifacts.artifactsHash";
    public static final String METADATA_RELEASE_ARTIFACTS_FIELD = "metadata.releaseArtifacts";
    public static final String METADATA_SUPPLY_CHAIN_PATH_FIELD = "metadata.supplyChainPath";
    public static final String COLLECTION_NAME = "fs.files";
    public static final String RELEASE_ARTIFACTS_FIELD = "releaseArtifacts";
    public static final String ARTIFACTS_HASH = "artifactsHash";
    public static final String HASHES = "hashes";
    public static final String SUPPLY_CHAIN_PATH_FIELD = "supplyChainPath";
    public static final String RELEASE_DATE_FIELD = "releaseDate";
    public static final String METADATA_FIELD = "metadata";
    
    private final GridFsTemplate gridFsTemplate;

    private final MongoTemplate mongoTemplate;

    private final ObjectMapper releaseFileJsonMapper;

    @SneakyThrows
    public ReleaseDossierMetaData storeRelease(ReleaseDossierMetaData releaseDossierMetaData, ReleaseDossier releaseDossier) {
        OffsetDateTime releaseDate = OffsetDateTime.now(ZoneOffset.UTC);
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            releaseFileJsonMapper.writeValue(outputStream, releaseDossier);
            try (InputStream inputStream = outputStream.toInputStream()) {
                String fileName = releaseDossierMetaData.getRelease().getQualifiedSupplyChainName() + "-" + releaseDossierMetaData.getRelease().getReleaseDate().toInstant().getEpochSecond() + ".json";
                ObjectId objectId = gridFsTemplate.store(inputStream, fileName, "application/json", releaseDossierMetaData);
                releaseDossierMetaData.getRelease().setDossierId(objectId);
                return releaseDossierMetaData;
            }
        }
    }

}
