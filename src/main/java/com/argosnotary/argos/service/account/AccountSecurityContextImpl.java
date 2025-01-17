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
package com.argosnotary.argos.service.account;

import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.argosnotary.argos.domain.account.Account;

@Component
public class AccountSecurityContextImpl implements AccountSecurityContext {

    @Override
    public Optional<Account> getAuthenticatedAccount() {
        return Optional.ofNullable(getSecurityContext().getAuthentication())
                .map(Authentication::getPrincipal)
                .map(ArgosUserDetails.class::cast)
                .map(ArgosUserDetails::getAccount);
    }

	@Override
	public SecurityContext getSecurityContext() {
		return SecurityContextHolder.getContext();
	}
}

