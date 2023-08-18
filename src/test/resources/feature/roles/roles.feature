#
# Argos Notary - A new way to secure the Software Supply Chain
#
# Copyright (C) 2019 - 2020 Rabobank Nederland
# Copyright (C) 2019 - 2021 Gerard Borst <gerard.borst@argosnotary.com>
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <https://www.gnu.org/licenses/>.
#

Feature: Organization

  Background:
    * url karate.properties['server.baseurl']   
    * call read('classpath:common.feature')
    * reset() 
    * def defaultTestData = call read('classpath:default-test-data.js')
    * def keyPair = defaultTestData.personalAccounts['default-pa1']
    * def pa1 = defaultTestData.personalAccounts['default-pa1']
    * def pa2 = defaultTestData.personalAccounts['default-pa2']
    * def pa3 = defaultTestData.personalAccounts['default-pa3']
    * def pa4 = defaultTestData.personalAccounts['default-pa4']
    * def defaultOrganizationId = defaultTestData.defaultOrganization.id;
    * def defaultProjectId = defaultTestData.defaultProject.id;
    * configure headers = call read('classpath:headers.js') { token: #(defaultTestData.ownerToken)}
    
  Scenario: store roleassignment on default project return 201
    Given path '/api/roles/'+defaultOrganizationId
    And request {'resourceId':#(defaultOrganizationId),'identityId':#(pa1.personalAccount.id),'role':{'permissions':['READ']}}
    When method POST
    Then status 201
    And match response == {'id':'#uuid','resourceId':#(defaultOrganizationId),'identityId':#(pa1.personalAccount.id),'role':{'permissions':['READ']}}
    
    * def expectedPermissions = ['LINK_ADD','ROLE_WRITE','READ','RELEASE','WRITE']
    Given path '/api/roles/'+defaultProjectId
    And request {'resourceId':#(defaultProjectId),'identityId':#(pa1.personalAccount.id),'role':{'permissions':#(expectedPermissions)}}
    When method POST
    Then status 201
    And match response == {'id':'#uuid','resourceId':#(defaultProjectId),'identityId':#(pa1.personalAccount.id),'role':{'permissions':#(^^expectedPermissions)}}
    
    * def rasId = response.id
    * configure headers = call read('classpath:headers.js') { token: #(pa1.token)}
    Given path '/api/roles/'+defaultProjectId
    When method GET
    Then status 200
    And match response == [{'id':'#uuid','resourceId':#(defaultProjectId),'identityId':#(pa1.personalAccount.id),'role':{'permissions':#(^^expectedPermissions)}}]
    
    Given path '/api/roles/'+defaultProjectId
    And request {'resourceId':#(defaultProjectId),'identityId':#(pa2.personalAccount.id),'role': {'permissions':['READ']}}
    When method POST
    Then status 201
    And match response == {'id':'#uuid','resourceId':#(defaultProjectId),'identityId':#(pa2.personalAccount.id),'role':{'permissions':['READ']}}
    
    Given path '/api/roles/'+defaultProjectId+'/roleassignments/'+rasId
    When method DELETE
    Then status 204
    
    Given path '/api/roles/'+defaultProjectId
    When method GET
    Then status 403
    And match response.messages[0].message == 'Access denied'
    
    * configure headers = call read('classpath:headers.js') { token: #(pa2.token)}
    Given path '/api/projects/'+defaultProjectId
    When method GET
    Then status 200
    And match response == {"id":#(defaultProjectId),"name":"default-project","pathToRoot":[#(defaultProjectId),#(defaultOrganizationId)],"parentId":#(defaultOrganizationId)}
  
  Scenario: store roleassignment with sc on project on management node on organization should return a 201 delete management node
    * def extraAccount = paLogin('user6')
    * def accountId = karate.toString(extraAccount.personalAccount.id)
    * configure headers = call read('classpath:headers.js') { token: #(extraAccount.token)}
    * def result = call read('classpath:feature/nodes/create-organization.feature') {org: { name: 'org1'}}
    * match result.response == { name: 'org1', id: '#uuid', 'pathToRoot':['#uuid'] }
    * def orgId = result.response.id
    
    * def expectedPermissions = ['LINK_ADD','ATTESTATION_ADD','ROLE_WRITE','READ','RELEASE','WRITE']
    Given path '/api/roles/'+orgId
    When method GET
    Then status 200
    And match response[0] == {'id':'#uuid','resourceId':#(orgId),'identityId':#(accountId),'role':{'permissions':#(^^expectedPermissions)}}
    
    Given path '/api/nodes/'+orgId+'/managementnodes'
    And request { name: 'node1', parentId: #(orgId)}
    When method POST
    Then status 201
    Then match response == {'id':'#uuid','name': 'node1', 'parentId':#(orgId), 'pathToRoot': [#(response.id), #(orgId)]}
    * def nodeId = response.id
    
    Given path '/api/nodes/'+response.id+'/projects'
    And request { name: 'project1', parentId: #(response.id)}
    When method POST
    Then status 201
    Then match response == {'id':'#uuid','name': 'project1', 'parentId':#(nodeId), 'pathToRoot': [#(response.id), #(nodeId), #(orgId)]}
    * def projectId = response.id
    
    Given path '/api/nodes/'+projectId+'/supplychains'
    And request { name: 'sc1', parentId: #(projectId)}
    When method POST
    Then status 201
    Then match response == {'id':'#uuid','name': 'sc1', 'parentId':#(projectId), 'pathToRoot': [#(response.id), #(projectId), #(nodeId), #(orgId)]}
    * def scId = response.id
    
    Given path '/api/roles/'+nodeId
    And request {'resourceId':#(nodeId),'identityId':#(pa1.personalAccount.id),'role':{'permissions':#(expectedPermissions)}}
    When method POST
    Then status 201
    
    * configure headers = call read('classpath:headers.js') { token: #(pa1.token)}
    Given path '/api/supplychains/'+scId
    When method GET
    Then status 200
    
    Given path '/api/managementnodes/'+nodeId
    When method DELETE
    Then status 204
    
    * configure headers = call read('classpath:headers.js') { token: #(extraAccount.token)}
    Given path '/api/roles/'+nodeId
    When method GET
    Then status 404
    Then match response.messages[0].message == 'Resource with id ['+nodeId+'] not found'
    
