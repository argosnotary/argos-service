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
package com.argosnotary.argos.service.security.oauth;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.argosnotary.argos.domain.account.Account;
import com.argosnotary.argos.domain.account.PersonalAccount;
import com.argosnotary.argos.domain.account.PersonalAccount.Profile;
import com.argosnotary.argos.service.account.AccountService;
import com.argosnotary.argos.service.account.ClientRegistrationService;
import com.argosnotary.argos.service.account.PersonalAccountService;
import com.argosnotary.argos.domain.account.ServiceAccount;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class OidcAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final CustomStatelessAuthorizationRequestRepository customStatelessAuthorizationRequestRepository;
    
    private final AccountService accountService;
    
    private final PersonalAccountService personalAccountService;
    
    private final ClientRegistrationService clientRegistrationService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OidcUser oidcUser = ((OidcUser) authentication.getPrincipal());
        
        processOidcUser(oidcUser);
        
        String tokenStr = oidcUser.getIdToken().getTokenValue();

        clearAuthenticationAttributes(request, response);
        response.setContentType("application/json");
        response.getOutputStream().print(tokenStr);
        response.getOutputStream().flush();
        response.getOutputStream().close();
    }

    protected void clearAuthenticationAttributes(HttpServletRequest request, HttpServletResponse response) {
        super.clearAuthenticationAttributes(request);
        customStatelessAuthorizationRequestRepository.removeAuthorizationRequestCookies(request, response);
    }
    
    private void processOidcUser(OidcUser oidcUser) {
        
    	Optional<Account> accountOptional =  accountService.loadAuthenticatedUser(oidcUser.getIssuer().toString(), oidcUser.getSubject());
        if((accountOptional.isPresent() && accountOptional.get() instanceof ServiceAccount)) {
        	// correct service account
        	return;
        }
        
        // existing or new Personal Account
        Profile profile = Profile.builder()
        		.familyName(oidcUser.getFamilyName())
        		.fullName(oidcUser.getFullName())
        		.givenName(oidcUser.getGivenName())
        		.build();
    	if (oidcUser.getEmailVerified()) {
    		profile.setEmail(oidcUser.getEmail());
    	}
    	
    	if (oidcUser.getPicture() != null) {
	        try {
				URL pictureUrl = new URL(oidcUser.getPicture());
				profile.setPicture(pictureUrl);
			} catch (MalformedURLException e) {
				logger.info(String.format("User: %s has a malformed picture url.", profile.getFullName()));
			}
    	}
        
        if(accountOptional.isPresent()) {
        	// existing Personal Account
        	PersonalAccount account = (PersonalAccount) accountOptional.get();
            if (account.getProfile() == null || !account.getProfile().equals(profile)) {
            	account.setProfile(profile);
            	personalAccountService.save(account);
            }
            
        } else {
        	// new Personal Account
            PersonalAccount oidcAccount = PersonalAccount.builder()
        			.name(oidcUser.getPreferredUsername())
    				.providerName(clientRegistrationService.getClientRegistrationName(oidcUser.getIssuer().toString()).get())
    				.providerSubject(oidcUser.getSubject())
    				.profile(profile)
    				.build();
            personalAccountService.save(oidcAccount);
        }
    }
    
}
