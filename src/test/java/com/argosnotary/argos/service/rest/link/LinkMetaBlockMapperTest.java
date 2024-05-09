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
package com.argosnotary.argos.service.rest.link;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.argosnotary.argos.domain.link.LinkMetaBlock;
import com.argosnotary.argos.service.openapi.rest.model.RestLinkMetaBlock;
import com.argosnotary.argos.service.rest.ArtifactMapperImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest(classes= {LinkMetaBlockMapperImpl.class, ArtifactMapperImpl.class})
class LinkMetaBlockMapperTest {

    @Autowired
	private LinkMetaBlockMapper converter;
    private ObjectMapper mapper;
    private String linkJson;

    @BeforeEach
    void setUp() throws IOException {
        mapper = new ObjectMapper();
        linkJson = IOUtils.toString(LinkMetaBlockMapperTest.class.getResource("/link.json"), StandardCharsets.UTF_8);
    }

    @Test
    void convertFromRestLinkMetaBlock() throws JsonProcessingException, JSONException {
        LinkMetaBlock link = converter.convertFromRestLinkMetaBlock(mapper.readValue(linkJson, RestLinkMetaBlock.class));
        JSONAssert.assertEquals(linkJson, mapper.writeValueAsString(converter.convertToRestLinkMetaBlock(link)), true);
    }
}
