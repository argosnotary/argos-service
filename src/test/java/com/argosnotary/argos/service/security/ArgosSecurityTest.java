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
package com.argosnotary.argos.service.security;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.UUID;

import org.apache.http.client.ClientProtocolException;
import org.bouncycastle.operator.OperatorCreationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.argosnotary.argos.service.ArgosTestContainers;
import com.argosnotary.argos.service.itest.rest.api.model.RestJwtToken;
import com.argosnotary.argos.service.itest.rest.api.model.RestKeyPair;
import com.argosnotary.argos.service.itest.rest.api.model.RestOAuthProvider;
import com.argosnotary.argos.service.itest.rest.api.model.RestTokenRequest;
import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlPasswordInput;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;

import lombok.extern.slf4j.Slf4j;
import com.argosnotary.argos.service.itest.rest.api.model.RestPersonalAccount;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
  properties={"spring.data.mongodb.auto-index-creation=true", "server.port=8081"})
@Testcontainers
class ArgosSecurityTest {
	
	static final String AUTH_HEADER_KEY= "Authorization";
	static final String AUTH_HEADER_BEARER_TEMPL= "Bearer %s";
	static final String SA_NAME = "0895000d-1f79-4f1b-91e4-12c9061cdbd3";
	static final String SA_PASSWORD = "test";
	
	static final String PA_NAME = "Luke";
	static final String PA_PASSWORD = "secret";
	
	WebTestClient client;

	public static String getKeycloakUrl(String realm) {
		String url = String.format("http://%s:%s/realms/%s", 
				keycloakContainer.getHost(), 
				keycloakContainer.getFirstMappedPort(),
				realm);
		return url;
	}
	
	public static String getKeycloakAuthUrl() {
		String url = String.format("http://%s:%s",
				keycloakContainer.getHost(), 
				keycloakContainer.getFirstMappedPort());
		return url;
	}
	
	@Container //
	private static GenericContainer keycloakContainer = ArgosTestContainers.getKeycloakContainer();

	
	static MongoDBContainer mongoDBContainer = ArgosTestContainers.getMongoDBContainer();
    
    static {
        mongoDBContainer.start();
    }

	@DynamicPropertySource
	static void setProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
		
		registry.add("spring.security.oauth2.client.provider.master.issuer-uri", () -> getKeycloakUrl("master"));// String.format("http://localhost:{}/realms/master", keycloakContainerPort));
		registry.add("spring.security.oauth2.client.provider.saprovider.issuer-uri", () -> getKeycloakUrl("saprovider"));
		registry.add("spring.security.oauth2.client.provider.oauth-stub.issuer-uri", () -> getKeycloakUrl("oauth-stub"));
	}
	
	@BeforeEach
	void init() throws ClientProtocolException, NoSuchAlgorithmException, OperatorCreationException, IOException {
		client = WebTestClient.bindToServer().baseUrl("http://localhost:8081").build();
	}

    @Test
    void testUnAuthenticated() {
    	client.get().uri("/actuator/health")
    	.accept(MediaType.APPLICATION_JSON)
    	.exchange()
    	.expectAll(
    		spec -> spec.expectStatus().isOk(),
    		spec -> spec.expectHeader().contentType(MediaType.APPLICATION_JSON)
    	);
    	
//    	client.get().uri("/swagger")
//    	.accept(MediaType.APPLICATION_JSON)
//    	.exchange()
//    	.expectAll(
//    		spec -> spec.expectStatus().isOk(),
//    		spec -> spec.expectHeader().contentType(MediaType.APPLICATION_JSON)
//    	);
    	
    	RestOAuthProvider ps = new RestOAuthProvider().name("oauth-stub");
    	
    	client.get().uri("/api/oauthprovider")
    	.accept(MediaType.APPLICATION_JSON)
    	.exchange()
    	.expectAll(
    		spec -> spec.expectStatus().isOk(),
    		spec -> spec.expectHeader().contentType(MediaType.APPLICATION_JSON),
    		spec -> spec.expectBodyList(RestOAuthProvider.class).hasSize(1).contains(ps)
    	);
    	
    	client.get().uri("/api/supplychains/verification/randomhash")
    	.accept(MediaType.APPLICATION_JSON)
    	.exchange()
    	.expectAll(
    		spec -> spec.expectStatus().isNotFound(),
    		spec -> spec.expectHeader().contentType(MediaType.APPLICATION_JSON)
    	);
    }
    
    @Test
    void testServiceAccount() {
    	RestTokenRequest req = new RestTokenRequest().accountId(UUID.fromString(SA_NAME)).passphrase(SA_PASSWORD);
    	// login
    	EntityExchangeResult<RestJwtToken> result = client.post().uri("/api/serviceaccounts/me/token")
    		//.header(AUTH_HEADER_KEY, authStr)
        	.accept(MediaType.APPLICATION_JSON)
        	.bodyValue(req)
    		.exchange()
    		.expectStatus().isOk()
    		.expectBody(RestJwtToken.class)
    		.returnResult();
    	
    	req = new RestTokenRequest().accountId(UUID.fromString(SA_NAME)).passphrase("bar");
    	client.post().uri("/api/serviceaccounts/me/token")
		.accept(MediaType.APPLICATION_JSON)
    	.bodyValue(req)
		.exchange()
		.expectStatus().isUnauthorized();
    }
    
    @Test
    void testCallAPiWithPa() throws ClientProtocolException, IOException {
    	String token = getToken(PA_NAME, PA_PASSWORD);
    	
    	// call api
    	String authStr = String.format(AUTH_HEADER_BEARER_TEMPL, token);
    	client.get().uri("/api/personalaccounts/me")
			.header(AUTH_HEADER_KEY, authStr)
	    	.accept(MediaType.APPLICATION_JSON)
			.exchange()
			.expectStatus().isOk()
			.expectBody()
    		.returnResult();
    	
    	client.get().uri("/api/personalaccounts/me")
    	    	.accept(MediaType.APPLICATION_JSON)
    			.exchange()
    			.expectStatus().isUnauthorized();
    }

    public String getToken(String userName, String password) throws ClientProtocolException, IOException {
    	String tokenStr = null;
    	try (WebClient webClient = new WebClient()) {
			webClient.getOptions().setRedirectEnabled(true);
			HtmlPage keycloakLoginPage = webClient.getPage("http://localhost:8081/oauth2/authorization/oauth-stub");
			// keycloak login form
			HtmlForm form = keycloakLoginPage.getFirstByXPath("//form");
			
			final HtmlPasswordInput passwordField = form.getInputByName("password");
			passwordField.type(password);

			HtmlTextInput userField = form.getInputByName("username");
			userField.type(userName);

			// Now submit the form by clicking the button and get back the second page.
			HtmlSubmitInput button = form.getInputByName("login");
			WebResponse response = button.click().getWebResponse();
			tokenStr = response.getContentAsString();
		} catch (FailingHttpStatusCodeException | ElementNotFoundException e) {
			throw e;
		}
    	return tokenStr;
    }
}
