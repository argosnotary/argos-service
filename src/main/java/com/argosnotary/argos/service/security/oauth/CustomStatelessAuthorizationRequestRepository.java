/*
 * Argos Notary - A new way to secure the Software Supply Chain
 *
 * Copyright (C) 2019 - 2020 Rabobank Nederland
 * Copyright (C) 2019 - 2022 Gerard Borst <gerard.borst@argosnotary.com>
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
import java.time.Duration;
import java.util.Base64;

import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

import com.argosnotary.argos.domain.ArgosError;
import com.argosnotary.argos.service.security.helpers.CookieHelper;
import com.argosnotary.argos.service.security.helpers.OAuth2AuthorizationRequestDeserializer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CustomStatelessAuthorizationRequestRepository implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    private static final Duration OAUTH_COOKIE_EXPIRY = Duration.ofMinutes(5);
    
    private static final ObjectMapper jsonMapper = new ObjectMapper();
    
    static {
    	 SimpleModule module = new SimpleModule();
    	 module.addDeserializer(OAuth2AuthorizationRequest.class, new OAuth2AuthorizationRequestDeserializer());
    	 jsonMapper.registerModule(module);
    }

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        return this.retrieveCookie(request);
    }

    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest, HttpServletRequest request, HttpServletResponse response) {
        if (authorizationRequest == null) {
            this.removeCookie(response);
            return;
        }
        this.attachCookie(response, authorizationRequest);
    }

	@Override
	public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request,
			HttpServletResponse response) {
        return this.retrieveCookie(request);
	}

    private OAuth2AuthorizationRequest retrieveCookie(HttpServletRequest request) {
        return CookieHelper.retrieve(request.getCookies(), CookieHelper.OAUTH_COOKIE_NAME)
                .map(this::decode)
                .orElse(null);
    }

    private void attachCookie(HttpServletResponse response, OAuth2AuthorizationRequest value) {
        Cookie cookie = CookieHelper.generate(CookieHelper.OAUTH_COOKIE_NAME, Base64.getEncoder().encodeToString(this.encode(value).getBytes()), OAUTH_COOKIE_EXPIRY);
        response.addCookie(cookie);
    }

    private void removeCookie(HttpServletResponse response) {
        Cookie expiredCookie = CookieHelper.generateExpiredCookie(CookieHelper.OAUTH_COOKIE_NAME);
        response.addCookie(expiredCookie);
    }
    
    public void removeAuthorizationRequestCookies(HttpServletRequest request, HttpServletResponse response) {
        CookieHelper.deleteCookie(request, response, CookieHelper.OAUTH_COOKIE_NAME);
    }
    
    private String encode(OAuth2AuthorizationRequest oAuth2AuthorizationRequest) {
			try {
				return jsonMapper.writeValueAsString(oAuth2AuthorizationRequest);
			} catch (JsonProcessingException e) {
				throw new ArgosError(e.getMessage(), e);
			}
    }

    private OAuth2AuthorizationRequest decode(String encoded) {
        try {
			return jsonMapper.readValue(new String(Base64.getDecoder().decode(encoded)), OAuth2AuthorizationRequest.class);
		} catch (IOException e) {
			throw new ArgosError(e.getMessage(), e);
		}
    }

}
