package com.argosnotary.argos.domain.release;

import static java.lang.String.join;
import static org.apache.commons.codec.digest.DigestUtils.sha256Hex;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.argosnotary.argos.domain.nodes.Organization;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
@Document(collection="releases")
@CompoundIndexes({
    @CompoundIndex(def = "{'organization.domain.domain' : 1}")
})
public class Release {
	@Id
	private UUID id;
    private OffsetDateTime releaseDate;
    private String qualifiedSupplyChainName;
    private Organization organization;
    @Indexed
    private UUID supplyChainId;
    @Indexed
    private Set<String> releasedProductsHashes;
    private ObjectId dossierId;

    @Indexed
    private String releasedProductsHashesHash;

    public static String calculateReleasedProductsHashesHash(Set<String> artifactList) {
        List<String> list = new ArrayList<>(artifactList);
        Collections.sort(list);
        return sha256Hex(join("", list));
    }

}
