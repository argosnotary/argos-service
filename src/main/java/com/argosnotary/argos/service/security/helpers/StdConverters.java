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
package com.argosnotary.argos.service.security.helpers;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.util.StdConverter;

import org.springframework.security.oauth2.core.AuthenticationMethod;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2AccessToken;

/**
 * {@code StdConverter} implementations.
 *
 * @author Joe Grandja
 * @since 5.3
 */
public abstract class StdConverters {
	private StdConverters() {}
	
	private static final String KEY_VALUE = "value";

	static final class AccessTokenTypeConverter extends StdConverter<JsonNode, OAuth2AccessToken.TokenType> {

		@Override
		public OAuth2AccessToken.TokenType convert(JsonNode jsonNode) {
			String value = JsonNodeUtils.findStringValue(jsonNode, KEY_VALUE);
			if (OAuth2AccessToken.TokenType.BEARER.getValue().equalsIgnoreCase(value)) {
				return OAuth2AccessToken.TokenType.BEARER;
			}
			return null;
		}

	}

	static final class ClientAuthenticationMethodConverter extends StdConverter<JsonNode, ClientAuthenticationMethod> {

		@Override
		public ClientAuthenticationMethod convert(JsonNode jsonNode) {
			String value = JsonNodeUtils.findStringValue(jsonNode, KEY_VALUE);
			if (ClientAuthenticationMethod.CLIENT_SECRET_BASIC.getValue().equalsIgnoreCase(value)) {
				return ClientAuthenticationMethod.CLIENT_SECRET_BASIC;
			}
			if (ClientAuthenticationMethod.CLIENT_SECRET_POST.getValue().equalsIgnoreCase(value)) {
				return ClientAuthenticationMethod.CLIENT_SECRET_POST;
			}
			if (ClientAuthenticationMethod.NONE.getValue().equalsIgnoreCase(value)) {
				return ClientAuthenticationMethod.NONE;
			}
			return null;
		}

	}

	static final class AuthorizationGrantTypeConverter extends StdConverter<JsonNode, AuthorizationGrantType> {

		@Override
		public AuthorizationGrantType convert(JsonNode jsonNode) {
			String value = JsonNodeUtils.findStringValue(jsonNode, KEY_VALUE);
			if (AuthorizationGrantType.AUTHORIZATION_CODE.getValue().equalsIgnoreCase(value)) {
				return AuthorizationGrantType.AUTHORIZATION_CODE;
			}
			if (AuthorizationGrantType.CLIENT_CREDENTIALS.getValue().equalsIgnoreCase(value)) {
				return AuthorizationGrantType.CLIENT_CREDENTIALS;
			}
			return new AuthorizationGrantType(value);
		}

	}

	static final class AuthenticationMethodConverter extends StdConverter<JsonNode, AuthenticationMethod> {

		@Override
		public AuthenticationMethod convert(JsonNode jsonNode) {
			String value = JsonNodeUtils.findStringValue(jsonNode, KEY_VALUE);
			if (AuthenticationMethod.HEADER.getValue().equalsIgnoreCase(value)) {
				return AuthenticationMethod.HEADER;
			}
			if (AuthenticationMethod.FORM.getValue().equalsIgnoreCase(value)) {
				return AuthenticationMethod.FORM;
			}
			if (AuthenticationMethod.QUERY.getValue().equalsIgnoreCase(value)) {
				return AuthenticationMethod.QUERY;
			}
			return null;
		}

	}

}

