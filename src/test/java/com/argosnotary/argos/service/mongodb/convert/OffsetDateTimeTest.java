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
package com.argosnotary.argos.service.mongodb.convert;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OffsetDateTimeTest {
	
    static final  String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SS'Z'";

	@BeforeEach
	void setUp() throws Exception {
	}

	@Test
	void testOffsetDateTimeReader() {
		OffsetDateTimeReadConverter r = new OffsetDateTimeReadConverter();
		assertNull(r.convert(null));
		
		assertEquals("2007-12-03T10:15:30Z",r.convert(Date.from(Instant.parse("2007-12-03T10:15:30.00Z"))).toString());
	}

	@Test
	void testOffsetDateTimeWriter() {
		OffsetDateTime t = OffsetDateTime.of(LocalDateTime.parse("1985-04-12T23:20:50.52Z", DateTimeFormatter.ofPattern(DATE_FORMAT)), ZoneOffset.UTC);
		OffSetDateTimeWriteConverter w = new OffSetDateTimeWriteConverter();
		assertNull(w.convert(null));
		Date d = w.convert(t);
		Date d2 = Date.from(Instant.parse("1985-04-12T23:20:50.52Z"));
		
		assertEquals(d2,w.convert(t));
	}

}
