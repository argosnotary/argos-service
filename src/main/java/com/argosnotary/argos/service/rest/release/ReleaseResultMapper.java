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
package com.argosnotary.argos.service.rest.release;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

import org.bson.types.ObjectId;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.springframework.beans.factory.annotation.Autowired;

import com.argosnotary.argos.domain.release.Release;
import com.argosnotary.argos.domain.release.ReleaseResult;
import com.argosnotary.argos.service.openapi.rest.model.RestRelease;
import com.argosnotary.argos.service.openapi.rest.model.RestReleaseResult;
import com.fasterxml.jackson.databind.ObjectMapper;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class ReleaseResultMapper {
	
	@Autowired
	ObjectMapper mapper;

    public abstract RestReleaseResult maptoRestReleaseResult(ReleaseResult releaseResult);
    
    public abstract RestRelease maptoRestRelease(Release release);
    
    public String offsetDateTimeIntoString(OffsetDateTime time) {
    	return time.toString();
    } 
    
    public  abstract List<String> mapToListString(Set<String> artifactHashes);
    
    public String objectIdIntoHexString(ObjectId id) {
    	return id.toHexString();
    }
    
    public ObjectId hexStringIntoObjectId(String hex) {
    	return new ObjectId(hex);
    }

}
