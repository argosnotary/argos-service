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
package com.argosnotary.argos.service.security.jwt;

import java.io.IOException;
import java.util.Optional;

import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.argosnotary.argos.domain.account.Account;
import com.argosnotary.argos.domain.account.PersonalAccount;
import com.argosnotary.argos.domain.account.ServiceAccount;
import com.argosnotary.argos.service.account.AccountService;
import com.argosnotary.argos.service.account.ArgosUserDetails;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor
public class AccountUserDetailsFilter  extends OncePerRequestFilter {

	private AccountService accountService;
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		SecurityContext context = SecurityContextHolder.getContext();
    	if(context.getAuthentication() != null && context.getAuthentication().getPrincipal() instanceof Jwt jwtUser) {
    		ArgosUserDetails userDetails = (ArgosUserDetails) loadUserByToken(jwtUser);
    		Authentication authenticatedPersonalAccount = new AccountAuthenticationToken(jwtUser, userDetails);
            authenticatedPersonalAccount.setAuthenticated(true);

			context.setAuthentication(authenticatedPersonalAccount);
    	}
    	
    	filterChain.doFilter(request, response);
	}
	
	UserDetails loadUserByToken(Jwt jwtUser) {
        Optional<Account> optionalAccount = accountService.loadAuthenticatedUser(jwtUser.getIssuer().toString(), jwtUser.getSubject());
    	if (optionalAccount.isEmpty()) {
    		log.warn("Login attempt with unknown username: [{}], issuer: [{}], subject: [{}]", jwtUser.getClaims().get("preferred_username"), jwtUser.getIssuer().toString(), jwtUser.getSubject());
    		throw new InternalAuthenticationServiceException("unknown account");
    	}
		if (optionalAccount.get() instanceof ServiceAccount) {
			return new ArgosUserDetails(optionalAccount.get());
		}
		PersonalAccount personalAccount = (PersonalAccount) optionalAccount.get();
	    return new ArgosUserDetails(personalAccount);
    }

}
