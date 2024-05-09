/*
 * Argos Notary - A new way to secure the Software Supply Chain
 *
 * Copyright (C) 2019 - 2020 Rabobank Nederland
 * Copyright (C) 2019 - 2024 Gerard Borst <gerard.borst@argosnotary.com>
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
package com.argosnotary.argos.service.rest.release;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.argosnotary.argos.service.JsonMapperConfig;
import com.fasterxml.jackson.core.JsonProcessingException;

@SpringBootTest(classes={ReleaseResultMapperImpl.class,JsonMapperConfig.class})
class ReleaseResultMapperTest {

    @Autowired
	private ReleaseResultMapper releaseResultMapper;
    
    @BeforeEach
	void setUp() throws Exception {
		//releaseResultMapper = new ReleaseResultMapperImpl();
	}

	@Test
	void testOffsetDateTimeIntoString() throws JsonProcessingException {
		OffsetDateTime time = OffsetDateTime.parse("2019-08-31T15:20:30+08:00");
		String timeStr = releaseResultMapper.offsetDateTimeIntoString(time);
		assertThat(timeStr, is("2019-08-31T15:20:30+08:00"));
	}
	
	@Test
	void testHexStringIntoObjectId() throws JsonProcessingException {
		String hex = "0123456789abcdef01234567";
		assertTrue(ObjectId.isValid(hex));
		ObjectId id = releaseResultMapper.hexStringIntoObjectId(hex);
		String res = releaseResultMapper.objectIdIntoHexString(id);
		assertThat(res, is("0123456789abcdef01234567"));
	}

}
