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
package com.argosnotary.argos.service.itest;

import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static com.mongodb.client.model.Filters.in;
import static com.mongodb.client.model.Filters.nin;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.bson.Document;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;
import org.bson.types.Binary;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Projections;
import com.mongodb.client.result.DeleteResult;



public class MongoDbClient {
	MongoClient mongoClient;
	MongoDatabase database;
	
	CodecProvider pojoCodecProvider = PojoCodecProvider.builder().automatic(true).build();
	CodecRegistry pojoCodecRegistry = fromRegistries(getDefaultCodecRegistry(), fromProviders(pojoCodecProvider));

	private static final Set<String> IGNORED_COLLECTIONS_FOR_ALL = Set.of(
			"mongockChangeLog", 
			"mongockLock", 
			"system.views",
			"accounts-keyinfo", 
			"accounts-info",
			"service-accounts-info-tmp", 
			"service-accounts-key-info-tmp");
	private static final Set<String> IGNORED_COLLECTIONS = new HashSet<>();

	protected static final String PERSONALACCOUNTS = "personalaccounts";

	static {
		IGNORED_COLLECTIONS.addAll(IGNORED_COLLECTIONS_FOR_ALL);
		IGNORED_COLLECTIONS.add(PERSONALACCOUNTS);
		IGNORED_COLLECTIONS.add("nodes");
		IGNORED_COLLECTIONS.add("serviceaccounts");
		IGNORED_COLLECTIONS.add("roleassignments");
	}

	public MongoDbClient(String url) {
		mongoClient = MongoClients.create(url);
		database = mongoClient.getDatabase("argos").withCodecRegistry(pojoCodecRegistry);;
	}

	public void resetNotAllRepositories() {
		deleteNotIgnored(IGNORED_COLLECTIONS);
		
		Bson query = nin("name", List.of(
				"luke", 
				"user1",
				"user2",
				"user3",
				"user4",
				"user5"
				));
		deleteFromColl(query, PERSONALACCOUNTS);
		
		query = in("name", List.of("default-organization", "default-project"));
		List<Binary> nodeIds = new ArrayList<>();
		database.getCollection("nodes").find(query).forEach(doc ->nodeIds.add((Binary)doc.get("_id")));
		
		query = nin("resourceId", nodeIds);
		deleteFromColl(query, "roleassignments");
		
		query = nin("name", List.of("default-organization", "default-project"));
		deleteFromColl(query, "nodes");
		
		query = nin("name", List.of("default-sa1", "default-sa2", "default-sa3", "default-sa4", "default-sa5"));
		deleteFromColl(query, "serviceaccounts");
	}
	
	public void resetAllRepositories() {
		deleteNotIgnored(IGNORED_COLLECTIONS_FOR_ALL);
    }
	
	private void deleteNotIgnored(Set<String> ignoredSet) {
		database.listCollectionNames().forEach(name -> {
			if (!ignoredSet.contains(name.trim())) {
				long aantal = database.getCollection(name).countDocuments();
				DeleteResult result = database.getCollection(name).deleteMany(new Document());
				long no = result.getDeletedCount();
				System.out.println(String.format("Deleted document count from collection %s with %d documents: %d", name, aantal, result.getDeletedCount()));
			}
		});		
	}
	
	private void deleteFromColl(Bson query, String collection) {
		DeleteResult result = database.getCollection(collection).deleteMany(query);
		long no = result.getDeletedCount();
		System.out.println(String.format("Deleted document count from collection %s : %d", collection, result.getDeletedCount()));		
	}
	
	public String getAuditLogs() {
		FindIterable<Document> logs = database.getCollection("auditlogs").find();
		List<String> logsList = new ArrayList<>();
        logs.forEach(l -> logsList.add(l.toJson()));
        return "[" + logsList.stream().collect(Collectors.joining(","))+ "]";
	}
	

	
//	private RestKeyPair getKeyPair(String keyId) {
//        return serviceAccountRepository
//                .findByActiveKeyId(keyId).map(serviceAccount -> (Account) serviceAccount)
//                .or(() -> personalAccountRepository.findByActiveKeyId(keyId)).map(Account::getActiveKeyPair);
//	}
//	
//	private Bson activeKeyIdQuery(String keyId) {
//        return eq("activeKeyPair.keyId", keyId);
//    }
//	
//	public RestPersonalAccount findByActiveKeyId(String activeKeyId) {
//        Optional<FindIterable<RestPersonalAccount>> pa =  Optional.ofNullable(database.getCollection(PERSONALACCOUNTS).find(activeKeyIdQuery(activeKeyId), RestPersonalAccount.class));
//    }
//	
//	public RestServiceAccount findByActiveKeyId(String activeKeyId) {
//        return Optional.ofNullable(template.findOne(getActiveKeyQuery(activeKeyId), ServiceAccount.class, COLLECTION));
//    }

//	    public void deletePersonalAccount(String accountId) {
//	        template.remove(new Query(Criteria.where("accountId").is(accountId)), PERSONALACCOUNTS);
//	    }

}
