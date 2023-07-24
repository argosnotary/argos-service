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
package com.argosnotary.argos.domain.account;

import java.net.URL;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.mongodb.core.mapping.Document;

import com.argosnotary.argos.domain.crypto.KeyPair;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper=true)
@ToString(callSuper=true)
@Document(collection="personalaccounts")
public class PersonalAccount extends Account {
	@Builder
    public PersonalAccount(
            UUID id,
            String providerName,
            String providerSubject,
            String name,
            Profile profile,
            KeyPair activeKeyPair,
            Set<KeyPair> inactiveKeyPairs
    ) {
        super(id,
        		providerName,
                providerSubject,
                name,
                activeKeyPair,
                inactiveKeyPairs);
        this.profile = profile;
    }
	
	private Profile profile;
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Profile {
        private String fullName;
        private String givenName;
        private String familyName;
        private String email;
        private URL picture;
    }

}
