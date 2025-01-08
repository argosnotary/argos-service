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
package com.argosnotary.argos.service.rest.attest;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import com.argosnotary.argos.domain.ArgosError;
import com.argosnotary.argos.domain.attest.Attestation;
import com.argosnotary.argos.domain.attest.Envelope;
import com.argosnotary.argos.domain.attest.Predicate;
import com.argosnotary.argos.domain.attest.ResourceDescriptor;
import com.argosnotary.argos.domain.attest.Statement;
import com.argosnotary.argos.domain.attest.predicate.provenance.Provenance;
import com.argosnotary.argos.domain.attest.predicate.provenance.RunDetails;
import com.argosnotary.argos.domain.attest.statement.InTotoStatement;
import com.argosnotary.argos.service.openapi.rest.model.RestAttestation;
import com.argosnotary.argos.service.openapi.rest.model.RestEnvelope;
import com.argosnotary.argos.service.openapi.rest.model.RestInTotoStatement;
import com.argosnotary.argos.service.openapi.rest.model.RestProvenance;
import com.argosnotary.argos.service.openapi.rest.model.RestResourceDescriptor;
import com.argosnotary.argos.service.openapi.rest.model.RestRunDetails;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AttestationMapper {

	DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SS'Z'");
	
	Attestation convertFromRestAttestation(RestAttestation restAttestation);
	
    Envelope convertFromRestStatementEnvelop(RestEnvelope restEnvelop);
	
    InTotoStatement convertFromRestInTotoStatement(RestInTotoStatement restInTotoStatement);
    
    List<ResourceDescriptor> mapRestResourceDescriptors(List<RestResourceDescriptor> rsList);
    
    Provenance convertFromRestProvenance(RestProvenance restProvenance);

    RestAttestation convertToRestAttestation(Attestation attestation);

    RestEnvelope convertToRestStatementEnvelope(Envelope envelope);

    default RestInTotoStatement convertToRestInTotoStatement(Statement statement) {
    	if (statement instanceof InTotoStatement inTotoStatement) {
    		return convertToRestInTotoStatement(inTotoStatement);
    	} else {
    		throw new ArgosError("Unknown statement type: "+statement.getClass().getCanonicalName());
    	}
    }
    
    RestInTotoStatement convertToRestInTotoStatement(InTotoStatement inTotoStatement);
    
    default RestProvenance convertToRestProvenance(Predicate predicate) {
    	if (predicate instanceof Provenance provenance) {
    		return convertToRestProvenance(provenance);
    	} else {
    		throw new ArgosError("Unknown predicate type: "+predicate.getClass().getCanonicalName());
    	}
    }
    
    RestProvenance convertToRestProvenance(Provenance provenance);
    
    RestRunDetails convertToRestRunDetails(RunDetails runDetails);
    
    default OffsetDateTime mapToOffsetDateTime(String dateTimeString) {
    	return OffsetDateTime.of(LocalDateTime.parse(dateTimeString, dateFormat), ZoneOffset.UTC);
    }
    
    default String mapToString(OffsetDateTime dateTime) {
    	return dateFormat.format(dateTime);
    }

}
