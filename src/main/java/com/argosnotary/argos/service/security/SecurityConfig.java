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
package com.argosnotary.argos.service.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtIssuerAuthenticationManagerResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.switchuser.SwitchUserFilter;

import com.argosnotary.argos.service.account.ClientRegistrationService;
import com.argosnotary.argos.service.security.jwt.AccountUserDetailsFilter;
import com.argosnotary.argos.service.security.oauth.CustomStatelessAuthorizationRequestRepository;
import com.argosnotary.argos.service.security.oauth.OidcAuthenticationFailureHandler;
import com.argosnotary.argos.service.security.oauth.OidcAuthenticationSuccessHandler;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {
	
	@Value("${spring.websecurity.debug:false}")
    boolean webSecurityDebug;
	
	//++++++++++++++++++++++++++++++
	// Anonymous filter chain
	//++++++++++++++++++++++++++++++

	@Bean(name = "anonFilterChain")
	@Order(SecurityProperties.BASIC_AUTH_ORDER - 100)
	public SecurityFilterChain anonFilterChain(HttpSecurity http) throws Exception {
		http.securityMatcher(
				"/swagger/**",
				"/actuator/**",
				"/api/supplychains/verification/**",
				"/api/oauthprovider/**",
				"/api/serviceaccounts/me/token")
			.authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll());
		http.csrf(csrf -> csrf.ignoringRequestMatchers("/api/serviceaccounts/me/token"));

		return http.build();
	}
	
	//++++++++++++++++++++++++++++++
	// Open Id Connect filter chain
	//++++++++++++++++++++++++++++++

	/*
	 * By default, Spring OAuth2 uses
	 * HttpSessionOAuth2AuthorizationRequestRepository to save the authorization
	 * request. But, since our service is stateless, we can't save it in the
	 * session. We'll save the request in a Base64 encoded cookie instead.
	 */
	@Bean
	public CustomStatelessAuthorizationRequestRepository cookieAuthorizationRequestRepository(ObjectMapper mapper) {
		return new CustomStatelessAuthorizationRequestRepository();
	}
	
	@Bean(name = "oidcFilterChain")
	@Order(SecurityProperties.BASIC_AUTH_ORDER - 90)
	public SecurityFilterChain oidcFilterChain(HttpSecurity http, 
			OidcAuthenticationSuccessHandler oidcAuthenticationSuccessHandler,
			OidcAuthenticationFailureHandler oidcAuthenticationFailureHandler,
			ObjectMapper mapper) throws Exception {
		http.securityMatcher(
				"/login/oauth2/**",
				"/oauth2/**");
		
		http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
		
		http.authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated());
		
		http.oauth2Login(oauth -> oauth
					.authorizationEndpoint(authorization -> authorization
					.authorizationRequestRepository(cookieAuthorizationRequestRepository(mapper)))
					.successHandler(oidcAuthenticationSuccessHandler)
					.failureHandler(oidcAuthenticationFailureHandler)
				);
		return http.build();
	}
	
	//++++++++++++++++++++++++++++++
	// API filter chain
	//++++++++++++++++++++++++++++++
	
	//++++++++++++++++++++++++++++++
	// JWT Authentication
	//++++++++++++++++++++++++++++++
	
	@Bean
	JwtIssuerAuthenticationManagerResolver authenticationManagerResolver(ClientRegistrationService clientRegistrationService) {

		return new JwtIssuerAuthenticationManagerResolver(clientRegistrationService.getAllOauthIssuers());
	}
	
	@Bean(name = "apiFilterChain")
	@Order(SecurityProperties.BASIC_AUTH_ORDER - 80)
	public SecurityFilterChain apiFilterChain(HttpSecurity http, AccountUserDetailsFilter accountUserDetailsFilter, ClientRegistrationService clientRegistrationService) throws Exception {
		http.securityMatcher("/api/**");
		
		http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
		
		http.authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated());
		
		http.oauth2ResourceServer(oauth2 -> oauth2
					.authenticationManagerResolver(authenticationManagerResolver(clientRegistrationService)));
		
		http.addFilterAfter(accountUserDetailsFilter, SwitchUserFilter.class);

        return http.build();
	}
	
}
