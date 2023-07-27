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

import java.io.IOException;

import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest.Builder;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.StdConverter;

/**
 * A {@code JsonDeserializer} for {@link OAuth2AuthorizationRequest}.
 *
 * @author Joe Grandja
 * @since 5.3
 * @see OAuth2AuthorizationRequest
 * @see OAuth2AuthorizationRequestMixin
 */
public final class OAuth2AuthorizationRequestDeserializer extends JsonDeserializer<OAuth2AuthorizationRequest> {

	private static final StdConverter<JsonNode, AuthorizationGrantType> AUTHORIZATION_GRANT_TYPE_CONVERTER = new StdConverters.AuthorizationGrantTypeConverter();

	@Override
	public OAuth2AuthorizationRequest deserialize(JsonParser parser, DeserializationContext context)
			throws IOException {
		ObjectMapper mapper = (ObjectMapper) parser.getCodec();
		JsonNode root = mapper.readTree(parser);
		return deserialize(parser, mapper, root);
	}

	private OAuth2AuthorizationRequest deserialize(JsonParser parser, ObjectMapper mapper, JsonNode root)
			throws JsonParseException {
		AuthorizationGrantType authorizationGrantType = AUTHORIZATION_GRANT_TYPE_CONVERTER
				.convert(JsonNodeUtils.findObjectNode(root, "grantType"));
		Builder builder = getBuilder(parser, authorizationGrantType);
		builder.authorizationUri(JsonNodeUtils.findStringValue(root, "authorizationUri"));
		builder.clientId(JsonNodeUtils.findStringValue(root, "clientId"));
		builder.redirectUri(JsonNodeUtils.findStringValue(root, "redirectUri"));
		builder.scopes(JsonNodeUtils.findValue(root, "scopes", JsonNodeUtils.STRING_SET, mapper));
		builder.state(JsonNodeUtils.findStringValue(root, "state"));
		builder.additionalParameters(
				JsonNodeUtils.findValue(root, "additionalParameters", JsonNodeUtils.STRING_OBJECT_MAP, mapper));
		builder.authorizationRequestUri(JsonNodeUtils.findStringValue(root, "authorizationRequestUri"));
		builder.attributes(JsonNodeUtils.findValue(root, "attributes", JsonNodeUtils.STRING_OBJECT_MAP, mapper));
		return builder.build();
	}

	private OAuth2AuthorizationRequest.Builder getBuilder(JsonParser parser,
			AuthorizationGrantType authorizationGrantType) throws JsonParseException {
		if (AuthorizationGrantType.AUTHORIZATION_CODE.equals(authorizationGrantType)) {
			return OAuth2AuthorizationRequest.authorizationCode();
		}
		throw new JsonParseException(parser, "Invalid authorizationGrantType");
	}

}

