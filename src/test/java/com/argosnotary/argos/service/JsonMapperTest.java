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
package com.argosnotary.argos.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@SpringBootTest(classes={JsonMapperConfig.class})
class JsonMapperTest {
	
	@Autowired
	JsonMapperConfig jsonMapperConfig;
	
	@Autowired
	ObjectMapper objectMapper;
	
	@Autowired
	JavaTimeModule module;

	@BeforeEach
	void setUp() throws Exception {
	}

	@Test
	void testWrite() throws JsonProcessingException {
		OffsetDateTime time = OffsetDateTime.parse("2023-08-13T18:00:27.006278874+02:00");
		
		String timeStr = objectMapper.writeValueAsString(time);
		assertEquals("\"2023-08-13T18:00:27.006278874+02:00\"", timeStr);
	}

	@Test
	void testNotNull() {
		JavaTimeModule m = jsonMapperConfig.dateTimeModule();
		JavaTimeModule nm = new JavaTimeModule();
		assertEquals(nm.getModuleName(), m.getModuleName());
		ObjectMapper o = jsonMapperConfig.objectMapper();
		assertFalse(o.isEnabled(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS));
		
	}
	
	@Test
	void someTests() throws JsonProcessingException {
		UUID id = UUID.randomUUID();
		String str = "testString";
		String idStr = objectMapper.writeValueAsString(id);
		assertEquals("\""+id.toString()+"\"", idStr);

		String strStr = objectMapper.writeValueAsString(str);
		assertEquals("\""+str+"\"", strStr);
		
	}
}
