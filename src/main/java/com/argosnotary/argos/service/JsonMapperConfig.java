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
package com.argosnotary.argos.service;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

import com.argosnotary.argos.service.security.helpers.OAuth2AuthorizationRequestDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Component
public class JsonMapperConfig {
    
    @Primary
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = JsonMapper.builder()
        		.addModule(new JavaTimeModule())
        		.addModule(oAuth2AuthorizationRequestModule())
        		.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        		.serializationInclusion(NON_NULL).build();

        return objectMapper;
    }
    
    @Bean
    public JavaTimeModule dateTimeModule(){
        return new JavaTimeModule();
    }
    
    @Bean
    public SimpleModule oAuth2AuthorizationRequestModule() {
    	SimpleModule module = new SimpleModule();
    	module.addDeserializer(OAuth2AuthorizationRequest.class, new OAuth2AuthorizationRequestDeserializer());
    	return module;
    }

}
