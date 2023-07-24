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

/*
 * Copyright 2002-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Utility class for {@code JsonNode}.
 *
 * @author Joe Grandja
 * @since 5.3
 */
public abstract class JsonNodeUtils {

	static final TypeReference<Set<String>> STRING_SET = new TypeReference<Set<String>>() {
	};

	static final TypeReference<Map<String, Object>> STRING_OBJECT_MAP = new TypeReference<Map<String, Object>>() {
	};

	static String findStringValue(JsonNode jsonNode, String fieldName) {
		if (jsonNode == null) {
			return null;
		}
		JsonNode value = jsonNode.findValue(fieldName);
		return (value != null && value.isTextual()) ? value.asText() : null;
	}

	static <T> T findValue(JsonNode jsonNode, String fieldName, TypeReference<T> valueTypeReference,
			ObjectMapper mapper) {
		if (jsonNode == null) {
			return null;
		}
		JsonNode value = jsonNode.findValue(fieldName);
		return (value != null && value.isContainerNode()) ? mapper.convertValue(value, valueTypeReference) : null;
	}

	static JsonNode findObjectNode(JsonNode jsonNode, String fieldName) {
		if (jsonNode == null) {
			return null;
		}
		JsonNode value = jsonNode.findValue(fieldName);
		return (value != null && value.isObject()) ? value : null;
	}

}

