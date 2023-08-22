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


import static com.argosnotary.argos.service.itest.ServiceClient.getOrganizationApi;
import static com.argosnotary.argos.service.itest.ServiceClient.getPersonalAccountApi;
import static com.argosnotary.argos.service.itest.ServiceClient.getProjectApi;
import static com.argosnotary.argos.service.itest.ServiceClient.getRoleAssignmentApi;
import static com.argosnotary.argos.service.itest.ServiceClient.getServiceAccountApi;
import static com.argosnotary.argos.service.itest.ServiceClient.getServiceAccountToken;
import static com.argosnotary.argos.service.itest.ServiceClient.getToken;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.http.client.ClientProtocolException;
import org.bouncycastle.operator.OperatorCreationException;

import com.argosnotary.argos.domain.roles.Permission;
import com.argosnotary.argos.domain.roles.Role;
import com.argosnotary.argos.service.itest.crypto.CryptoHelper;
import com.argosnotary.argos.service.itest.rest.api.client.PersonalAccountApi;
import com.argosnotary.argos.service.itest.rest.api.client.RoleAssignmentApi;
import com.argosnotary.argos.service.itest.rest.api.client.ServiceAccountApi;
import com.argosnotary.argos.service.itest.rest.api.model.RestDomain;
import com.argosnotary.argos.service.itest.rest.api.model.RestKeyPair;
import com.argosnotary.argos.service.itest.rest.api.model.RestManagementNode;
import com.argosnotary.argos.service.itest.rest.api.model.RestOrganization;
import com.argosnotary.argos.service.itest.rest.api.model.RestPermission;
import com.argosnotary.argos.service.itest.rest.api.model.RestPersonalAccount;
import com.argosnotary.argos.service.itest.rest.api.model.RestProject;
import com.argosnotary.argos.service.itest.rest.api.model.RestRole;
import com.argosnotary.argos.service.itest.rest.api.model.RestRoleAssignment;
import com.argosnotary.argos.service.itest.rest.api.model.RestServiceAccount;
import com.argosnotary.argos.service.itest.rest.api.model.RestServiceAccountKeyPair;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class DefaultTestData {
    private String ownerToken;
    private Map<String, TestPersonalAccount> personalAccounts = new HashMap<>();
    private RestOrganization defaultOrganization;
    private RestManagementNode defaultManagementNode;
    private RestProject defaultProject;
    private Map<String, TestServiceAccount> serviceAccounts = new HashMap<>();

    private static final String OWNER_USER = "luke";
    private static final String PASSPHRASE = "secret";
    private static final String KEY_PASSPHRASE = "test";
    private static Properties properties = Properties.getInstance();
    
    public DefaultTestData() throws ClientProtocolException, IOException, NoSuchAlgorithmException, OperatorCreationException {
    	this.ownerToken = getToken(OWNER_USER, PASSPHRASE);
    	this.defaultOrganization = createDefaultOrganization(ownerToken);
    	this.defaultProject = createDefaultProject(this.ownerToken, this.defaultOrganization.getId());
    	this.personalAccounts = createDefaultPersonalAccount(this.ownerToken, this.defaultOrganization);
    	this.serviceAccounts = createDefaultSaAccounts(this.personalAccounts.get("default-pa1").token, this.defaultProject.getId());
    	
    }
    
    @Builder
    @Getter
    @Setter
    public static class TestPersonalAccount {
        private RestPersonalAccount personalAccount;
        private String token;
        private String passphrase;
    }
    
    @Getter
    @Setter
    @AllArgsConstructor
    private static class TestKeyPair {
        private RestKeyPair keyPair;
        private String passphrase;
    }

    @Builder
    @Getter
    @Setter
    public static class TestServiceAccount {
        private RestServiceAccount serviceAccount;
        private String token;
        private String passphrase;
    }

    private static RestOrganization createDefaultOrganization(String ownerToken) {
    	RestOrganization org = new RestOrganization()
    			.name("default-organization")
    			.domain(new RestDomain().name("org.com"));
    	return getOrganizationApi(ownerToken).createOrganization(org);
    }

    private static RestProject createDefaultProject(String ownerToken, UUID parentId) {
    	RestProject node = new RestProject();
    	node.setName("default-project");
    	node.setParentId(parentId);
    	return getProjectApi(ownerToken).createProject(parentId, node);
    }

    private static Map<String, TestPersonalAccount> createDefaultPersonalAccount(String ownerToken, RestOrganization defaultOrganization) throws ClientProtocolException, IOException, NoSuchAlgorithmException, OperatorCreationException {
    	Map<String, TestPersonalAccount> personalAccounts =  new HashMap<>();
    	personalAccounts.put("default-pa"+1, createDefaultPersonalAccount(1, ownerToken, defaultOrganization, Set.of(new Role.Contributor()) ));
    	personalAccounts.put("default-pa"+2, createDefaultPersonalAccount(2, ownerToken, defaultOrganization, Set.of(new Role.Reader())));
    	personalAccounts.put("default-pa"+3, createDefaultPersonalAccount(3, ownerToken, defaultOrganization, Set.of(new Role.Contributor())));
    	personalAccounts.put("default-pa"+4, createDefaultPersonalAccount(4, ownerToken, defaultOrganization, Set.of()));
    	personalAccounts.put("default-pa"+5, createDefaultPersonalAccount(5, ownerToken, defaultOrganization, Set.of(new Role.LinkAdder(), new Role.AttestationAdder())));
    	return personalAccounts;
    }
    
    private static TestPersonalAccount createDefaultPersonalAccount(int userNo, String ownerToken, RestOrganization defaultOrganization, Set<Role> roles) throws ClientProtocolException, IOException, NoSuchAlgorithmException, OperatorCreationException {
    	String userName = "user"+userNo;
    	
    	TestPersonalAccount account = createTestPersonalAccount(userName);
    	
    	if (roles == null) {
    		return account;
    	}

    	RoleAssignmentApi roleAssignmentApi = getRoleAssignmentApi(ownerToken);
    	
    	roles.forEach(r -> {
    		List<RestPermission> lp = r.getPermissions().stream().map(p -> RestPermission.valueOf(p.name())).toList();
        	RestRole restRole = new RestRole().permissions(lp);
        	RestRoleAssignment ra = new RestRoleAssignment() ;
        	ra.setIdentityId(account.getPersonalAccount().getId());
        	ra.setResourceId(defaultOrganization.getId());
        	ra.setRole(restRole);
        	RestRoleAssignment res = roleAssignmentApi.createRoleAssignment(defaultOrganization.getId(), ra);
    	});
    	return account;
    	
    }
    
    public static TestPersonalAccount createTestPersonalAccount(String userName) throws ClientProtocolException, IOException, NoSuchAlgorithmException, OperatorCreationException {
    	String userToken = getToken(userName, PASSPHRASE);
    	PersonalAccountApi personalAccountApi = getPersonalAccountApi(userToken);
    	RestKeyPair kp = CryptoHelper.createKeyPair(KEY_PASSPHRASE.toCharArray());
    	TestKeyPair keyPair = new TestKeyPair(kp, KEY_PASSPHRASE);
    	personalAccountApi.createKey(keyPair.getKeyPair());
    	RestPersonalAccount user = personalAccountApi.whoAmI(); //.getPersonalAccountOfAuthenticatedUser();
    	return TestPersonalAccount.builder()
    			.personalAccount(user)
    			.passphrase(keyPair.getPassphrase())
    			.token(userToken)
    			.build();
    }
    
    private static Map<String, TestServiceAccount> createDefaultSaAccounts(String token, UUID projectId) throws ClientProtocolException, IOException, NoSuchAlgorithmException, OperatorCreationException {
    	Map<String, TestServiceAccount> accounts =  new HashMap<>();
    	accounts.put("default-sa1", createSaWithActiveKey("default-sa1", token, projectId));
    	accounts.put("default-sa2", createSaWithActiveKey("default-sa2", token, projectId));
    	accounts.put("default-sa3", createSaWithActiveKey("default-sa3", token, projectId));
        return accounts;
    }

    private static TestServiceAccount createSaWithActiveKey(String userName, String paToken, UUID projectId ) throws ClientProtocolException, IOException, NoSuchAlgorithmException, OperatorCreationException {
    	RestKeyPair kp = CryptoHelper.createKeyPair(KEY_PASSPHRASE.toCharArray());
    	TestKeyPair keyPair = new TestKeyPair(kp, KEY_PASSPHRASE);
    	RestServiceAccount sa = new RestServiceAccount();
    	sa.setProjectId(projectId);
    	sa.setName(userName);
        ServiceAccountApi serviceAccountApi = getServiceAccountApi(paToken);
        sa = serviceAccountApi.createServiceAccount(projectId, sa);
        
        String passphrase = keyPair.getPassphrase();
        RestServiceAccountKeyPair rkp = new RestServiceAccountKeyPair()
			.keyId(kp.getKeyId())
	        .passphrase(KEY_PASSPHRASE)
	        .encryptedPrivateKey(kp.getEncryptedPrivateKey())
	        .pub(kp.getPub());
        serviceAccountApi.createServiceAccountKeyById(projectId, sa.getId(), rkp);
                
        sa.activeKeyPair(keyPair.getKeyPair());
        

        String token = getServiceAccountToken(sa.getId(), passphrase);
        
        return TestServiceAccount.builder()
                		.serviceAccount(sa)
                        .passphrase(keyPair.getPassphrase())
                        .token(token)
                        .build();
    }
    
    private static TestKeyPair readKeyPair(int index) {
        try {
			return new ObjectMapper().readValue(DefaultTestData.class.getResourceAsStream("/itest/testmessages/key/default-test-keypair" + index + ".json"), TestKeyPair.class);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return null;
    }

}

