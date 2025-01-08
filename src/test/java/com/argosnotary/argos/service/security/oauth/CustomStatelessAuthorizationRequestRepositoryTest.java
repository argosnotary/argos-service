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
package com.argosnotary.argos.service.security.oauth;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.jackson2.SecurityJackson2Modules;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.util.SerializationUtils;

import com.argosnotary.argos.domain.ArgosError;
import com.argosnotary.argos.service.JsonMapperConfig;
import com.argosnotary.argos.service.security.helpers.CookieHelper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
class CustomStatelessAuthorizationRequestRepositoryTest {

    @Mock
    private CookieHelper cookieHelper;
    
    JsonMapperConfig jsonMapperConfig = new JsonMapperConfig();
	
	private ObjectMapper mapper = jsonMapperConfig.objectMapper();

    private CustomStatelessAuthorizationRequestRepository repository;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    private OAuth2AuthorizationRequest oAuth2AuthorizationRequest;
    
    private String base64OauthRequest;
    
    private Cookie[] cookies = new Cookie[1];
    
    private String strCcookie; 

    @BeforeEach
    void setUp() throws JsonProcessingException {
        oAuth2AuthorizationRequest = OAuth2AuthorizationRequest.authorizationCode().authorizationUri("http://some").clientId("is").build();
        repository = new CustomStatelessAuthorizationRequestRepository();
        JsonMapper jsonMapper = JsonMapper.builder()
        		.addModules(SecurityJackson2Modules.getModules(this.getClass().getClassLoader())).build();
        base64OauthRequest = Base64.getEncoder().encodeToString(jsonMapper.writeValueAsBytes(oAuth2AuthorizationRequest));
        cookies[0] = CookieHelper.generate(CookieHelper.OAUTH_COOKIE_NAME, base64OauthRequest, Duration.ofMinutes(5));
    }

    @Test
    void loadAuthorizationRequestFound() throws JsonProcessingException {
    	when(request.getCookies()).thenReturn(cookies);
    	OAuth2AuthorizationRequest req = repository.loadAuthorizationRequest(request);
    	assertEquals(oAuth2AuthorizationRequest.getAuthorizationRequestUri(), req.getAuthorizationRequestUri());
    	assertEquals(oAuth2AuthorizationRequest.getAuthorizationUri(), req.getAuthorizationUri());
    	assertEquals(oAuth2AuthorizationRequest.getClientId(), req.getClientId());
    }

    @Test
    void loadAuthorizationRequestNotFound() {
        OAuth2AuthorizationRequest oAuth2AuthorizationRequest = repository.loadAuthorizationRequest(request);
        assertThat(oAuth2AuthorizationRequest == null, is(true));
    }

    @Test
    void saveAuthorizationRequestNull() {
        repository.saveAuthorizationRequest(null, request, response);
        verify(response).addCookie(CookieHelper.generateExpiredCookie("OAUTH"));
    }

    @Test
    void saveAuthorizationRequestNotNull() {
        repository.saveAuthorizationRequest(oAuth2AuthorizationRequest, request, response);
        verify(response).addCookie(cookies[0]);
        //verify(request).getCookies();
        //verify(response).addCookie(CookieHelper.generate("OAUTH", "-", Duration.ZERO));
    }

    @Test
    void removeAuthorizationRequest() {
        when(request.getCookies()).thenReturn(cookies);
        assertThat(SerializationUtils.serialize(repository.removeAuthorizationRequest(request, response)), is(SerializationUtils.serialize(oAuth2AuthorizationRequest)));
    }
    
    @Test
    void removeAuthorizationRequestCookiesTest() {
        when(request.getCookies()).thenReturn(cookies);
        repository.removeAuthorizationRequestCookies(request, response);
        verify(response).addCookie(CookieHelper.generateExpiredCookie("OAUTH"));
    }
    
    @Test
    void decodeExceptionTest() {
    	String cookie = Base64.getEncoder().encodeToString("aCookie".getBytes());
        Cookie[] cookies = {CookieHelper.generate(CookieHelper.OAUTH_COOKIE_NAME, cookie, Duration.ofMinutes(5))};
    	when(request.getCookies()).thenReturn(cookies);		

        Throwable exception = assertThrows(ArgosError.class, () -> {
        	repository.loadAuthorizationRequest(request);
          });
        
        assertEquals("Unrecognized token 'aCookie': was expecting (JSON String, Number, Array, Object or token 'null', 'true' or 'false')\n"
        		+ " at [Source: REDACTED (`StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION` disabled); line: 1, column: 8]", exception.getMessage());
		
    }
    
    @Test
    void encodeExceptionTest() {
    	Map<String, Object> additionalParameters = new HashMap<>();
    	additionalParameters.put("bla", new Object());
    	OAuth2AuthorizationRequest auth = OAuth2AuthorizationRequest.authorizationCode().additionalParameters(additionalParameters).authorizationUri("http://some").clientId("is").build();		

        Throwable exception = assertThrows(ArgosError.class, () -> {
        	repository.saveAuthorizationRequest(auth, request, response);
          });
        
        assertEquals("No serializer found for class java.lang.Object and no properties discovered to create BeanSerializer (to avoid exception, disable SerializationFeature.FAIL_ON_EMPTY_BEANS) (through reference chain: org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest[\"additionalParameters\"]->java.util.Collections$UnmodifiableMap[\"bla\"])", exception.getMessage());
    	
    }
}