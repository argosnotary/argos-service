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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.argosnotary.argos.domain.account.PersonalAccount;

@ExtendWith(MockitoExtension.class)
class AccountSecurityContextTest {
	
	private AccountSecurityContext accountSecurityContext;
	
	@Mock
	private SecurityContext securityContext;
	@Mock
	private ArgosUserDetails argosUserDetails;
	@Mock
	private Authentication authentication;
	
	private PersonalAccount pa;

	@BeforeEach
	void setUp() throws Exception {
		
		accountSecurityContext = new AccountSecurityContextImpl();
		SecurityContextHolder.setContext(securityContext);
		pa = PersonalAccount.builder().name("pa").build();
		argosUserDetails = new ArgosUserDetails(pa);
		
	}

	@Test
	void testGetAuthenticatedAccount() {
		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(authentication.getPrincipal()).thenReturn(argosUserDetails);
		assertEquals(Optional.of(pa), accountSecurityContext.getAuthenticatedAccount());
		
		
	}

}
