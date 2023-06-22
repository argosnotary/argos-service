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

import static org.springframework.data.mongodb.core.query.MongoRegexCreator.MatchMode.STARTING_WITH;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.MongoRegexCreator;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Component;

import com.argosnotary.argos.domain.ArgosError;
import com.argosnotary.argos.domain.release.ReleaseDossier;
import com.argosnotary.argos.domain.release.ReleaseDossierMetaData;
import com.argosnotary.argos.service.NotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.gridfs.model.GridFSFile;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReleaseRepositoryImpl implements ReleaseRepository {

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
    @Override
    public ReleaseDossierMetaData storeRelease(ReleaseDossierMetaData releaseDossierMetaData, ReleaseDossier releaseDossier) {
        OffsetDateTime releaseDate = OffsetDateTime.now(ZoneOffset.UTC);
        releaseDossierMetaData.setReleaseDate(releaseDate);
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            releaseFileJsonMapper.writeValue(outputStream, releaseDossier);
            try (InputStream inputStream = outputStream.toInputStream()) {
                String fileName = "release-" + releaseDossierMetaData.getSupplyChainPath() + "-" + releaseDate.toInstant().getEpochSecond() + ".json";
                ObjectId objectId = gridFsTemplate.store(inputStream, fileName, "application/json", releaseDossierMetaData);
                releaseDossierMetaData.setDocumentId(objectId.toHexString());
                return releaseDossierMetaData;
            }
        }
    }

    @Override
    public Optional<ReleaseDossierMetaData> findReleaseByReleasedArtifactsAndPath(List<List<String>> releasedArtifacts, String path) {
        checkForEmptyArtifacts(releasedArtifacts);

        Criteria criteria = createArtifactCriteria(releasedArtifacts);

        addOptionalPathCriteria(path, criteria);

        Query query = new Query(criteria);
        log.info("findReleaseByReleasedArtifactsAndPath: {}", query);
        List<ReleaseDossierMetaData> releaseDossierMetaData = mongoTemplate.find(query, ReleaseDossierMetaData.class, COLLECTION_NAME);

        if (releaseDossierMetaData.size() > 1) {
            throw new NotFoundException("no unique release was found please specify a supply chain path parameter");
        } else if (releaseDossierMetaData.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(releaseDossierMetaData.iterator().next());
        }

    }

    private void checkForEmptyArtifacts(List<List<String>> releasedArtifacts) {
        if (releasedArtifacts.isEmpty()) {
            throw new ArgosError("releasedArtifacts cannot be empty", ArgosError.Level.WARNING);
        }
    }
    
    private void addOptionalPathCriteria(String path, Criteria criteria) {
        if (path != null) {
            criteria.and(METADATA_SUPPLY_CHAIN_PATH_FIELD)
                    .regex(Objects.requireNonNull(MongoRegexCreator.INSTANCE
                            .toRegularExpression(path, STARTING_WITH)));
        }
    }
    
    private void addOptionalPathListCriteria(List<String> paths, Criteria criteria) {
        if (paths != null && !paths.isEmpty()) {
            List<Criteria> pathListCriteria = new ArrayList<>();
            paths.forEach(p -> pathListCriteria.add(Criteria.where(METADATA_SUPPLY_CHAIN_PATH_FIELD)
            .regex(Objects.requireNonNull(MongoRegexCreator.INSTANCE
                    .toRegularExpression(p, STARTING_WITH)))));
            
            Criteria pathCriteria = new Criteria();
            pathCriteria.orOperator(pathListCriteria.toArray(new Criteria[0]));
            
            criteria.andOperator(pathCriteria);
        }
    }

    private Criteria createArtifactCriteria(List<List<String>> releasedArtifacts) {
        Criteria criteria = new Criteria();
        List<Criteria> hashCriteria = new ArrayList<>();
        releasedArtifacts.forEach(l -> hashCriteria.add(createListHashCriteria(l)));
        criteria.andOperator(hashCriteria.toArray(new Criteria[0]));
        return criteria;

    }

    @SneakyThrows
    @Override
    public Optional<String> getRawReleaseFileById(String id) {
        GridFSFile file = gridFsTemplate.findOne(new Query(Criteria.where(ID_FIELD).is(id)));
        assert file != null;
        String releaseFileJson = IOUtils.toString(gridFsTemplate.getResource(file).getInputStream(), StandardCharsets.UTF_8.name());
        return Optional.ofNullable(releaseFileJson);
    }
    
    private Criteria createListHashCriteria(List<String> releasedArtifacts) {
        String artifactsHash = ReleaseDossierMetaData.createHashFromArtifactList(releasedArtifacts);
        return Criteria.where(METADATA_RELEASE_ARTIFACTS_ARTIFACTS_HASH_FIELD).is(artifactsHash);
    }

    @Override
    public boolean artifactsAreReleased(List<String> releasedArtifacts, List<String> paths) {
        Criteria criteria = createListHashCriteria(releasedArtifacts);
        addOptionalPathListCriteria(paths, criteria);
        Query query = new Query(criteria);
        log.info("artifactsAreReleased: {}", query);
        long noOfReleases = mongoTemplate.count(query, ReleaseDossierMetaData.class, COLLECTION_NAME);
        return (noOfReleases == 1) || (noOfReleases > 1 && (paths != null && !paths.isEmpty()));
    }

}
