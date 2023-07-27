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

import java.io.Serializable;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;

import com.argosnotary.argos.domain.crypto.KeyPair;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper=true)
@ToString(callSuper=true)
@CompoundIndex(name = "providerSubject_providerName", def = "{'providerSubject' : 1, 'providerName': 1}", unique=true)
public abstract class Account extends Identity implements Serializable {
	private String providerName;
	
    private String providerSubject;
    private KeyPair activeKeyPair;
    private Set<KeyPair> inactiveKeyPairs;

	Account(UUID id,
			String providerName,
		    String providerSubject, 
		    String name, 
		    KeyPair activeKeyPair, 
		    Set<KeyPair> inactiveKeyPairs) {
		super(id, name);
		this.providerName = providerName;
		this.providerSubject = providerSubject;
		this.activeKeyPair = activeKeyPair;
		this.inactiveKeyPairs = inactiveKeyPairs == null ? new HashSet<>() : inactiveKeyPairs;
	}
	
    public void deactivateKeyPair(KeyPair newKeyPair) {
        Optional.ofNullable(this.getActiveKeyPair()).ifPresent(keyPair -> {
            Set<KeyPair> inactivePairs = Optional.ofNullable(this.getInactiveKeyPairs()).orElse(new HashSet<>());
            inactivePairs.add(keyPair);
            this.setActiveKeyPair(null);
            this.setInactiveKeyPairs(inactivePairs);
        });
        this.setActiveKeyPair(newKeyPair);
    }
}
