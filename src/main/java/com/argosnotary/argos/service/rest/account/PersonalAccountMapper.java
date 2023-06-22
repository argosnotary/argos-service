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
package com.argosnotary.argos.service.rest.account;

import java.util.Optional;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import com.argosnotary.argos.domain.account.PersonalAccount;
import com.argosnotary.argos.domain.account.PersonalAccount.Profile;
import com.argosnotary.argos.service.openapi.rest.model.RestPersonalAccount;
import com.argosnotary.argos.service.openapi.rest.model.RestProfile;
import com.fasterxml.jackson.databind.ObjectMapper;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PersonalAccountMapper {
	
	ObjectMapper mapper = new ObjectMapper();

    public RestPersonalAccount convertToRestPersonalAccount(PersonalAccount personalAccount);
    
    default public RestProfile convertToRestProfile(Optional<Profile> profile) {
    	if (profile.isEmpty()) {
    		return null;
    	}
    	return convertToRestProfile(profile.get());
    }
    
    public RestProfile convertToRestProfile(Profile profile);

    @Mapping(target = "profile", ignore = true)
    public abstract RestPersonalAccount convertToRestPersonalAccountIdentity(PersonalAccount personalAccount);

}
