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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import com.argosnotary.argos.domain.account.PersonalAccount;
import com.argosnotary.argos.domain.account.PersonalAccount.Profile;
import com.argosnotary.argos.service.account.AccountService;
import com.argosnotary.argos.service.account.ClientRegistrationService;
import com.argosnotary.argos.service.account.PersonalAccountService;
import com.argosnotary.argos.domain.account.ServiceAccount;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


@ExtendWith(MockitoExtension.class)
class OidcAuthenticationSuccessHandlerTest {
	private static final UUID ACCOUNT_ID = UUID.randomUUID();

    @Mock
    private CustomStatelessAuthorizationRequestRepository customStatelessAuthorizationRequestRepository;
    
    @Mock
    private AccountService accountService;
    
    @Mock
    private PersonalAccountService personalAccountService;
    
    @Mock
    private ClientRegistrationService clientRegistrationService;
    
    @Mock
    private ObjectMapper objectMapper;

    private OidcAuthenticationSuccessHandler successHandler;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private Authentication authentication;

    @Mock
    private OidcUser oidcUser;

    @Mock
    private ServiceAccount serviceAccount;

    private PersonalAccount personalAccount1;
    
    private Profile profile1;
    
    private Profile profile2;

    private PersonalAccount personalAccount2;

    private PersonalAccount personalAccount3;

    @Mock
    private OidcIdToken idToken;

    @Mock
    private ServletOutputStream servletOutputStream;


    @BeforeEach
    void setUp() throws MalformedURLException {
        successHandler = new OidcAuthenticationSuccessHandler(customStatelessAuthorizationRequestRepository, accountService,personalAccountService,clientRegistrationService);
        profile1 = Profile.builder()
        		.familyName("theFamilyName")
        		.fullName("theFullName")
        		.givenName("theGivenName")
        		.email("a@b.nl")
        		.picture(new URL("http://aPicture"))
        		.build();
        profile2 = Profile.builder()
        		.familyName("theFamilyName")
        		.fullName("theFullName")
        		.givenName("theOtherGivenName")
        		.email("a@b.nl")
        		.picture(new URL("http://aPicture"))
        		.build();
        personalAccount1 = PersonalAccount.builder().profile(profile1).build();
        personalAccount2 = PersonalAccount.builder().id(personalAccount1.getId()).build();
        personalAccount3 = PersonalAccount.builder().id(personalAccount1.getId()).profile(profile2).build();
    }

    @Test
    void onAuthenticationSuccess() throws IOException, ServletException {
        when(authentication.getPrincipal()).thenReturn(oidcUser);
        when(oidcUser.getIssuer()).thenReturn(new URL("http://oidcIssuer"));
        when(oidcUser.getSubject()).thenReturn("idToken");
        when(oidcUser.getIdToken()).thenReturn(idToken);
        when(idToken.getTokenValue()).thenReturn("token");
        when(response.getOutputStream()).thenReturn(servletOutputStream);
        when(clientRegistrationService.getClientRegistrationName("http://oidcIssuer")).thenReturn(Optional.of("providerName"));
        successHandler.onAuthenticationSuccess(request, response, authentication);
        verify(customStatelessAuthorizationRequestRepository).removeAuthorizationRequestCookies(request, response);
        verify(servletOutputStream).print("token");
        verify(request).getSession(false);

    }
    


    @Test
    void onAuthenticationSuccessPersonalAccountNoChange() throws IOException, ServletException {
        when(authentication.getPrincipal()).thenReturn(oidcUser);
        when(oidcUser.getIssuer()).thenReturn(new URL("http://oidcIssuer"));
        when(oidcUser.getSubject()).thenReturn("idToken");
        when(oidcUser.getIdToken()).thenReturn(idToken);
        when(oidcUser.getFamilyName()).thenReturn(profile1.getFamilyName());
        when(oidcUser.getFullName()).thenReturn(profile1.getFullName());
        when(oidcUser.getGivenName()).thenReturn(profile1.getGivenName());
        when(oidcUser.getEmail()).thenReturn(profile1.getEmail());
        when(oidcUser.getPicture()).thenReturn(profile1.getPicture().toString());
        when(oidcUser.getEmailVerified()).thenReturn(true);
        when(idToken.getTokenValue()).thenReturn("token");
        when(response.getOutputStream()).thenReturn(servletOutputStream);
        when(accountService.loadAuthenticatedUser(oidcUser.getIssuer().toString(), oidcUser.getSubject())).thenReturn(Optional.of(personalAccount1));
        when(response.getOutputStream()).thenReturn(servletOutputStream);
        successHandler.onAuthenticationSuccess(request, response, authentication);
        verify(customStatelessAuthorizationRequestRepository).removeAuthorizationRequestCookies(request, response);
        verify(servletOutputStream).print("token");
        verify(request).getSession(false);
        verify(personalAccountService, never()).save(personalAccount1);

    }
    
    @Test
    void onAuthenticationSuccessPersonalAccountProfileEmpty() throws IOException, ServletException {
        when(authentication.getPrincipal()).thenReturn(oidcUser);
        when(oidcUser.getIssuer()).thenReturn(new URL("http://oidcIssuer"));
        when(oidcUser.getSubject()).thenReturn("idToken");
        when(oidcUser.getIdToken()).thenReturn(idToken);
        when(oidcUser.getFamilyName()).thenReturn(profile1.getFamilyName());
        when(oidcUser.getFullName()).thenReturn(profile1.getFullName());
        when(oidcUser.getGivenName()).thenReturn(profile1.getGivenName());
        when(oidcUser.getEmail()).thenReturn(profile1.getEmail());
        when(oidcUser.getPicture()).thenReturn(profile1.getPicture().toString());
        when(oidcUser.getEmailVerified()).thenReturn(true);
        when(idToken.getTokenValue()).thenReturn("token");
        when(response.getOutputStream()).thenReturn(servletOutputStream);
        when(accountService.loadAuthenticatedUser(oidcUser.getIssuer().toString(), oidcUser.getSubject())).thenReturn(Optional.of(personalAccount2));
        when(response.getOutputStream()).thenReturn(servletOutputStream);
        successHandler.onAuthenticationSuccess(request, response, authentication);
        verify(customStatelessAuthorizationRequestRepository).removeAuthorizationRequestCookies(request, response);
        verify(servletOutputStream).print("token");
        verify(request).getSession(false);
        verify(personalAccountService).save(personalAccount1);

    }
    
    @Test
    void onAuthenticationSuccessPersonalAccountProfileChange() throws IOException, ServletException {
        when(authentication.getPrincipal()).thenReturn(oidcUser);
        when(oidcUser.getIssuer()).thenReturn(new URL("http://oidcIssuer"));
        when(oidcUser.getSubject()).thenReturn("idToken");
        when(oidcUser.getIdToken()).thenReturn(idToken);
        when(oidcUser.getFamilyName()).thenReturn(profile2.getFamilyName());
        when(oidcUser.getFullName()).thenReturn(profile2.getFullName());
        when(oidcUser.getGivenName()).thenReturn(profile2.getGivenName());
        when(oidcUser.getEmail()).thenReturn(profile2.getEmail());
        when(oidcUser.getPicture()).thenReturn(profile2.getPicture().toString());
        when(oidcUser.getEmailVerified()).thenReturn(true);
        when(idToken.getTokenValue()).thenReturn("token");
        when(response.getOutputStream()).thenReturn(servletOutputStream);
        when(accountService.loadAuthenticatedUser(oidcUser.getIssuer().toString(), oidcUser.getSubject())).thenReturn(Optional.of(personalAccount1));
        when(response.getOutputStream()).thenReturn(servletOutputStream);
        successHandler.onAuthenticationSuccess(request, response, authentication);
        verify(customStatelessAuthorizationRequestRepository).removeAuthorizationRequestCookies(request, response);
        verify(servletOutputStream).print("token");
        verify(request).getSession(false);
        verify(personalAccountService).save(personalAccount3);

    }
    
    @Test
    void onAuthenticationSuccessPersonalAccountNew() throws IOException, ServletException {
        when(authentication.getPrincipal()).thenReturn(oidcUser);
        when(oidcUser.getIssuer()).thenReturn(new URL("http://oidcIssuer"));
        when(oidcUser.getSubject()).thenReturn("idToken");
        when(oidcUser.getIdToken()).thenReturn(idToken);
        when(oidcUser.getFamilyName()).thenReturn(profile1.getFamilyName());
        when(oidcUser.getFullName()).thenReturn(profile1.getFullName());
        when(oidcUser.getGivenName()).thenReturn(profile1.getGivenName());
        when(oidcUser.getEmail()).thenReturn(profile1.getEmail());
        when(oidcUser.getPicture()).thenReturn(profile1.getPicture().toString());
        when(oidcUser.getEmailVerified()).thenReturn(true);
        when(idToken.getTokenValue()).thenReturn("token");
        when(response.getOutputStream()).thenReturn(servletOutputStream);
        when(accountService.loadAuthenticatedUser(oidcUser.getIssuer().toString(), oidcUser.getSubject())).thenReturn(Optional.empty());
        when(clientRegistrationService.getClientRegistrationName("http://oidcIssuer")).thenReturn(Optional.of("providerName"));
        when(response.getOutputStream()).thenReturn(servletOutputStream);
        successHandler.onAuthenticationSuccess(request, response, authentication);
        verify(customStatelessAuthorizationRequestRepository).removeAuthorizationRequestCookies(request, response);
        verify(servletOutputStream).print("token");
        verify(request).getSession(false);
        verify(personalAccountService).save(any());

    }
    


    @Test
    void onAuthenticationSuccessServiceAccount() throws IOException, ServletException {
        when(authentication.getPrincipal()).thenReturn(oidcUser);
        when(oidcUser.getIssuer()).thenReturn(new URL("http://oidcIssuer"));
        when(oidcUser.getSubject()).thenReturn("idToken");
        when(oidcUser.getIdToken()).thenReturn(idToken);
        when(idToken.getTokenValue()).thenReturn("token");
        when(response.getOutputStream()).thenReturn(servletOutputStream);
        when(accountService.loadAuthenticatedUser(oidcUser.getIssuer().toString(), oidcUser.getSubject())).thenReturn(Optional.of(serviceAccount));
        successHandler.onAuthenticationSuccess(request, response, authentication);
        verify(customStatelessAuthorizationRequestRepository).removeAuthorizationRequestCookies(request, response);
        verify(servletOutputStream).print("token");
        verify(request).getSession(false);
        verify(oidcUser, never()).getEmailVerified();

    }
}