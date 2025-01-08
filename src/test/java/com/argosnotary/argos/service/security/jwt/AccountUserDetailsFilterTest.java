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
package com.argosnotary.argos.service.security.jwt;

import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import com.argosnotary.argos.domain.account.PersonalAccount;
import com.argosnotary.argos.service.account.AccountSecurityContext;
import com.argosnotary.argos.service.account.AccountSecurityContextImpl;
import com.argosnotary.argos.service.account.AccountService;
import com.argosnotary.argos.service.account.ArgosUserDetails;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
class AccountUserDetailsFilterTest {
	
	private AccountSecurityContext accountSecurityContext;
	
	AccountUserDetailsFilter filter;
	
	private SecurityContext securityContext;
	
	private ArgosUserDetails argosUserDetails;
	
	private Authentication jwtAuthentication;

    @Mock
	private AccountService accountService;
	
	@Mock
    private HttpServletRequest httpServletRequest;
	
	@Mock
    private HttpServletResponse httpServletResponse;
	
	private Map<String, Object> claims = new HashMap<>();
	
	@Mock
	Jwt jwtUser;
	
	@Mock
	FilterChain filterChain;
	
	private PersonalAccount pa;

	@BeforeEach
	void setUp() throws Exception {
		accountSecurityContext = new AccountSecurityContextImpl();
		securityContext = new SecurityContextImpl();
		SecurityContextHolder.setContext(securityContext);
		assertThat(accountSecurityContext.getSecurityContext(), sameInstance(securityContext));
		pa = PersonalAccount.builder().name("pa").build();
		argosUserDetails = new ArgosUserDetails(pa);
		filter = new AccountUserDetailsFilter(accountService, accountSecurityContext);
		claims.put("preferred_username", "Gerard");
	}

	@Test
	void testNotAuthenticated() throws ServletException, IOException {
		filter.doFilter(httpServletRequest, httpServletResponse, filterChain);
		verify(filterChain).doFilter(httpServletRequest, httpServletResponse);
		assertNull(securityContext.getAuthentication());
	}

	@Test
	void testJwtUser() throws ServletException, IOException {
		Authentication auth = new JwtAuthenticationToken(jwtUser, null, null);
		URL issuer = new URL("http://theissuer");
		securityContext.setAuthentication(auth);
		when(jwtUser.getIssuer()).thenReturn(issuer);
		when(jwtUser.getSubject()).thenReturn("theSubject");
		when(accountService.loadAuthenticatedUser(issuer.toString(), "theSubject")).thenReturn(Optional.of(pa));
		filter.doFilter(httpServletRequest, httpServletResponse, filterChain);
		verify(filterChain).doFilter(httpServletRequest, httpServletResponse);
		assertTrue(securityContext.getAuthentication().isAuthenticated());
		ArgosUserDetails details = (ArgosUserDetails) securityContext.getAuthentication().getPrincipal();
		assertEquals(pa, details.getAccount() );
	}

	@Test
	void testNoJwtuser() throws ServletException, IOException {
		Authentication auth = new UsernamePasswordAuthenticationToken("user", "pass");
		securityContext.setAuthentication(auth);
		filter.doFilter(httpServletRequest, httpServletResponse, filterChain);
		verify(filterChain).doFilter(httpServletRequest, httpServletResponse);
		assertThat(securityContext.getAuthentication(), sameInstance(auth));
	}

	@Test
	void testAccountNotKnown() throws ServletException, IOException {
		Authentication auth = new JwtAuthenticationToken(jwtUser, null, null);
		URL issuer = new URL("http://theissuer");
		securityContext.setAuthentication(auth);
		when(jwtUser.getIssuer()).thenReturn(issuer);
		when(jwtUser.getSubject()).thenReturn("theSubject");
		when(jwtUser.getClaims()).thenReturn(claims);
		when(accountService.loadAuthenticatedUser(issuer.toString(), "theSubject")).thenReturn(Optional.empty());

		Throwable exception = assertThrows(InternalAuthenticationServiceException.class, () -> {
			filter.doFilter(httpServletRequest, httpServletResponse, filterChain);
        });
		assertEquals("unknown account", exception.getMessage());
	}

}
