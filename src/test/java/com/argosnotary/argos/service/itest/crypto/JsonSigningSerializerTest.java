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
package com.argosnotary.argos.service.itest.crypto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.argosnotary.argos.service.itest.rest.api.model.RestArgosDigest;
import com.argosnotary.argos.service.itest.rest.api.model.RestBuildDefinition;
import com.argosnotary.argos.service.itest.rest.api.model.RestBuilder;
import com.argosnotary.argos.service.itest.rest.api.model.RestDigest;
import com.argosnotary.argos.service.itest.rest.api.model.RestInTotoStatement;
import com.argosnotary.argos.service.itest.rest.api.model.RestMetaData;
import com.argosnotary.argos.service.itest.rest.api.model.RestProvenance;
import com.argosnotary.argos.service.itest.rest.api.model.RestResourceDescriptor;
import com.argosnotary.argos.service.itest.rest.api.model.RestRunDetails;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;

class JsonSigningSerializerTest {
	
	@BeforeEach
	void setUp() throws Exception {
	}

	@Test
	void test() throws StreamReadException, DatabindException, IOException, URISyntaxException {

		RestMetaData m = new RestMetaData()
				.invocationId("theInvocationId")
				.startedOn("1985-04-12T23:20:50.52Z")
				.finishedOn("1985-04-12T23:25:50.52Z"); 
		RestResourceDescriptor gitCommit = new RestResourceDescriptor()
				.uri(new URI("https://github.com/argosnotary/argos-service/commit/86b64f3da76f56e46f800a80945ac8fdf67719e4")).argosDigest(new RestArgosDigest().hash("86b64f3da76f56e46f800a80945ac8fdf67719e4"));
		RestResourceDescriptor desc = new RestResourceDescriptor().uri(new URI("package.jar")).argosDigest(new RestArgosDigest().hash("956c0ea99f13ef0c866c87f1afd457d798198e96ad0561dee7d59d4e95444484")).digest(new RestDigest().sha256("956c0ea99f13ef0c866c87f1afd457d798198e96ad0561dee7d59d4e95444484"));
		RestBuildDefinition bd = new RestBuildDefinition().resolvedDependencies(List.of(gitCommit));
		RestProvenance p = new RestProvenance().buildDefinition(bd)
				.runDetails(new RestRunDetails().metadata(m).builder(new RestBuilder().version(null).builderDependencies(List.of(gitCommit)))); // /, m, null))
		RestInTotoStatement st = new RestInTotoStatement().predicate(p).predicateType("https://slsa.dev/provenance/v1").subject(List.of(desc)).type("https://in-toto.io/Statement/v1");
		String restStatementJson = new JsonSigningSerializer().serialize(st);
		assertEquals("{\"_type\":\"https://in-toto.io/Statement/v1\",\"predicate\":{\"buildDefinition\":{\"resolvedDependencies\":[{\"argosDigest\":{\"hash\":\"86b64f3da76f56e46f800a80945ac8fdf67719e4\"},\"uri\":\"https://github.com/argosnotary/argos-service/commit/86b64f3da76f56e46f800a80945ac8fdf67719e4\"}]},\"runDetails\":{\"builder\":{\"builderDependencies\":[{\"argosDigest\":{\"hash\":\"86b64f3da76f56e46f800a80945ac8fdf67719e4\"},\"uri\":\"https://github.com/argosnotary/argos-service/commit/86b64f3da76f56e46f800a80945ac8fdf67719e4\"}]},\"metadata\":{\"finishedOn\":\"1985-04-12T23:25:50.52Z\",\"invocationId\":\"theInvocationId\",\"startedOn\":\"1985-04-12T23:20:50.52Z\"}}},\"predicateType\":\"https://slsa.dev/provenance/v1\",\"subject\":[{\"argosDigest\":{\"hash\":\"956c0ea99f13ef0c866c87f1afd457d798198e96ad0561dee7d59d4e95444484\"},\"digest\":{\"sha256\":\"956c0ea99f13ef0c866c87f1afd457d798198e96ad0561dee7d59d4e95444484\"},\"uri\":\"package.jar\"}]}",restStatementJson);
	}

}
