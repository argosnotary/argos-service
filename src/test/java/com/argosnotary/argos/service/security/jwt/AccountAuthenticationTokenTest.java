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
package com.argosnotary.argos.service.security.jwt;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;

@ExtendWith(MockitoExtension.class)
class AccountAuthenticationTokenTest {
	
	@Mock
	Jwt jwtUser;
	
	@Mock
	UserDetails principal;

	@BeforeEach
	void setUp() throws Exception {
	}

	@Test
	void testConstructor() {
		AccountAuthenticationToken token = new AccountAuthenticationToken(jwtUser, principal);
		verify(principal).getAuthorities();
		
		assertThat(token.getToken(), sameInstance(jwtUser));
		assertThat(token.getPrincipal(), sameInstance(principal));
		
	}
	
	@Test
	void testGetCredential() {
		AccountAuthenticationToken token = new AccountAuthenticationToken(jwtUser, principal);
		when(jwtUser.toString()).thenReturn("jwtUser");
		assertEquals("jwtUser", token.getCredentials());
	}

}
