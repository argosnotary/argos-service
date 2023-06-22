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
package com.argosnotary.argos.service.itest;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

import org.apache.http.client.ClientProtocolException;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.pkcs.PKCSException;

import com.argosnotary.argos.service.itest.DefaultTestData.TestPersonalAccount;
import com.argosnotary.argos.service.itest.crypto.CryptoHelper;
import com.fasterxml.jackson.core.JsonProcessingException;

public class KarateClient {
	
	public static TestPersonalAccount paLogin(String userName) throws ClientProtocolException, IOException, NoSuchAlgorithmException, OperatorCreationException {
		TestPersonalAccount acc = DefaultTestData.createTestPersonalAccount(userName);
		return acc;
    }
	
	public static String signLayout(String password, HashMap keyPairJson, HashMap restLayoutMetaBlockJson) throws JsonProcessingException, OperatorCreationException, GeneralSecurityException, IOException, PKCSException {
		CryptoHelper helper = new CryptoHelper();
		return helper.signLayout(password, keyPairJson, restLayoutMetaBlockJson);
	}
	
	public static String signLink(String password, HashMap keyPairJson, HashMap restLinkMetaBlockJson) throws JsonProcessingException, OperatorCreationException, GeneralSecurityException, IOException, PKCSException {
		CryptoHelper helper = new CryptoHelper();
		return helper.signLink(password, keyPairJson, restLinkMetaBlockJson);
	}
	
	public static String getAuditLogs() {
		MongoDbClient client = new MongoDbClient(System.getProperty("mongo-url"));
		return client.getAuditLogs();
	}
	
	public static void resetNotAllRepositories() {
		MongoDbClient client = new MongoDbClient(System.getProperty("mongo-url"));
		client.resetNotAllRepositories();
	}

}
