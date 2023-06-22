package com.argosnotary.argos.service.rest.account;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import com.argosnotary.argos.domain.crypto.KeyPair;
import com.argosnotary.argos.domain.crypto.PublicKey;
import com.argosnotary.argos.service.openapi.rest.model.RestKeyPair;
import com.argosnotary.argos.service.openapi.rest.model.RestPublicKey;
import com.argosnotary.argos.service.openapi.rest.model.RestServiceAccountKeyPair;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface KeyPairMapper {

    public KeyPair convertFromRestKeyPair(RestKeyPair restKeyPair);
    
    public RestKeyPair convertToRestKeyPair(KeyPair keyPair);

    public RestPublicKey convertToRestPublicKey(PublicKey publicKey);
    
    public KeyPair convertFromRestServiceAccountKeyPair(RestServiceAccountKeyPair restKeyPair);

}
