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
package com.argosnotary.argos.service.mongodb;

import java.util.Collection;
import java.util.Collections;

import org.bson.UuidRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions.MongoConverterConfigurationAdapter;

import com.argosnotary.argos.service.mongodb.convert.OffSetDateTimeWriteConverter;
import com.argosnotary.argos.service.mongodb.convert.OffsetDateTimeReadConverter;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

@Configuration
public class MongoConfig extends AbstractMongoClientConfiguration {
    
    @Override
    protected void configureConverters(@Autowired MongoConverterConfigurationAdapter adapter) { 

    	adapter.registerConverter(new OffsetDateTimeReadConverter());
    	adapter.registerConverter(new OffSetDateTimeWriteConverter());
    }

    @Value("${spring.data.mongodb.uri}")
    private String mongoURI;
    
    @Value("${spring.data.mongodb.database}")
    private String mongoDatabaseName;
    
    @Bean
    public MongoTransactionManager transactionManager(MongoDatabaseFactory dbFactory) {
        return new MongoTransactionManager(dbFactory);
    }
    
    public MongoClientSettings mongoClientSettings(String mongoURI) {
    	return MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(mongoURI))
        		.uuidRepresentation(UuidRepresentation.STANDARD)
                .build();
    }
    
    @Override
    public MongoClient mongoClient() {
        return MongoClients.create(mongoClientSettings(mongoURI));
    }

    @Override
    protected String getDatabaseName() {
        return mongoDatabaseName;
    }
    
    @Override
    public Collection<String> getMappingBasePackages() {
        return Collections.singleton("com.argosnotary.argos.domain");
    }
}
