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
package com.argosnotary.argos.service.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;

import org.aspectj.util.Reflection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.argosnotary.argos.service.rest.nodes.OrganizationRestServiceImpl;

class MethodArgumentTypeMismatchExceptionHandlerTest {
    
    MethodArgumentTypeMismatchExceptionHandler exceptionHandler;


	@BeforeEach
	void setUp() throws Exception {
		exceptionHandler = new MethodArgumentTypeMismatchExceptionHandler();
	}

	@Test
	void testHandleMethodArgumentTypeMismatch() throws Exception {
		Object[] args = {UUID.randomUUID()};
		MethodArgumentTypeMismatchException exp = new MethodArgumentTypeMismatchException("foo", UUID.class, "method.name", new MethodParameter(Reflection.getMatchingMethod(OrganizationRestServiceImpl.class, "getOrganizations", args), 0), new Exception("MostSpecificCause"));
		String error = exceptionHandler.handleMethodArgumentTypeMismatch(exp);
		assertEquals("{\"messages\":[{\"field\":\"getOrganizations.method.name\",\"type\":\"DATA_INPUT\",\"message\":\"MostSpecificCause\"}]}", error);
		
	}

}
