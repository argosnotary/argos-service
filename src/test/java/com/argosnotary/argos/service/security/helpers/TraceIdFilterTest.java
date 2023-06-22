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
package com.argosnotary.argos.service.security.helpers;

import static org.mockito.Mockito.verify;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

@ExtendWith(MockitoExtension.class)
class TraceIdFilterTest {
    @Mock
    private LogContextHelper logContextHelper;

    @Mock
    private ServletRequest servletRequest;
    @Mock
    private ServletResponse servletResponse;
    @Mock
    private FilterChain filterChain;

    private TraceIdFilter traceIdFilter;

    @BeforeEach
    void setUp() {
        traceIdFilter = new TraceIdFilter(logContextHelper);
    }

    @Test
    void doFilter() throws IOException, ServletException {
        traceIdFilter.doFilter(servletRequest, servletResponse, filterChain);
        verify(logContextHelper).addTraceIdToLogContext();
        verify(filterChain).doFilter(servletRequest, servletResponse);
    }
}