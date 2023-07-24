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
package com.argosnotary.argos.service.itest;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.awaitility.Awaitility.await;

import java.io.IOException;
import java.net.http.HttpRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import org.apache.commons.io.IOUtils;
import org.apache.commons.text.StringSubstitutor;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

import com.argosnotary.argos.service.itest.rest.api.ApiClient;
import com.argosnotary.argos.service.itest.rest.api.client.OrganizationApi;
import com.argosnotary.argos.service.itest.rest.api.client.PersonalAccountApi;
import com.argosnotary.argos.service.itest.rest.api.client.ProjectApi;
import com.argosnotary.argos.service.itest.rest.api.client.RoleAssignmentApi;
import com.argosnotary.argos.service.itest.rest.api.client.ServiceAccountApi;
import com.argosnotary.argos.service.itest.rest.api.client.SupplyChainApi;
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

@Slf4j
public class ServiceClient {
	public static final String SERVER_BASEURL = "server.baseurl";
    
    private static Properties properties = Properties.getInstance();
    
    public static void waitForArgosServiceToStart() {
        log.info("Waiting for argos service start");
        HttpClient client = HttpClientBuilder.create().build();
        await().atMost(1, MINUTES).until(() -> {
            try {
                HttpResponse send = client.execute(new HttpGet(System.getProperty(SERVER_BASEURL) + "/actuator/health"));
                return 200 == send.getStatusLine().getStatusCode();
            } catch (IOException e) {
                //ignore
                return false;
            }
        });

        log.info("argos service started");
    }

    private static String createBodyResponse(String name, String lastName, String email) throws IOException {
        String bodyTemplate = IOUtils.toString(ServiceClient.class
                .getResourceAsStream("/testmessages/authentication/response.json"), UTF_8);

        Map<String, String> values = new HashMap<>();
        values.put("name", name);
        values.put("lastName", lastName);
        values.put("email", email);
        values.put("id", UUID.randomUUID().toString());
        return StringSubstitutor.replace(bodyTemplate, values, "${", "}");
    }

    public static String getToken(String userName, String password, String authorizationUri) throws ClientProtocolException, IOException {
    	String tokenStr = null;
    	try (WebClient webClient = new WebClient()) {
			webClient.getOptions().setRedirectEnabled(true);
			HtmlPage keycloakLoginPage = webClient.getPage(properties.getApiBaseUrl() + authorizationUri);
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

    public static OrganizationApi getOrganizationApi(String bearerToken) {
        return new OrganizationApi(getApiClient(bearerToken));
    }
    
    public static ProjectApi getProjectApi(String bearerToken) {
        return new ProjectApi(getApiClient(bearerToken));
    }
    
    public static SupplyChainApi getSupplyChainApi(String bearerToken) {
        return new SupplyChainApi(getApiClient(bearerToken));
    }
    
    public static RoleAssignmentApi getRoleAssignmentApi(String bearerToken) {
        return new RoleAssignmentApi(getApiClient(bearerToken));
    }

    public static PersonalAccountApi getPersonalAccountApi(String bearerToken) {
        return new PersonalAccountApi(getApiClient(bearerToken));
    }

    public static ServiceAccountApi getServiceAccountApi(String bearerToken) {
        return new ServiceAccountApi(getApiClient(bearerToken));
    }
    
    
    
    private static ApiClient getApiClient(String bearerToken) {
        ApiClient apiClient = new ApiClient();
        // scheme + "://" + host + (port == -1 ? "" : ":" + port) + basePath
        apiClient.setScheme("http");
        apiClient.setHost("localhost");
        apiClient.setPort(8080);
        apiClient.setBasePath("/api");
        apiClient.setRequestInterceptor(new Consumer<HttpRequest.Builder>() {
        	  @Override
        	  public void accept(HttpRequest.Builder builder) {
        	    builder.header("Authorization", "Bearer " + bearerToken);
        	  }
        	});
        
        return apiClient;
    }

}
