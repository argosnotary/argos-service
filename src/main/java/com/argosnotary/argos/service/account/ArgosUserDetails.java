/*
 * Argos Notary - A new way to secure the Software Supply Chain
 *
 * Copyright (C) 2019 - 2020 Rabobank Nederland
 * Copyright (C) 2019 - 2024 Gerard Borst <gerard.borst@argosnotary.com>
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
package com.argosnotary.argos.service.account;


import java.util.List;
import java.util.UUID;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import com.argosnotary.argos.domain.account.Account;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public class ArgosUserDetails extends User {
	
	private final Account account;

    public ArgosUserDetails(Account account) {
        super(account.getName(), "", List.of(new SimpleGrantedAuthority("ROLE_USER")));
        this.account = account;
    }

    public UUID getId() {
        return account.getId();
    }

    public Account getAccount() {
        return account;
    }
}
