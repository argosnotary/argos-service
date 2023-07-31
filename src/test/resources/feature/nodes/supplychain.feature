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

Feature: Project

  Background:
    * url karate.properties['server.baseurl']   
    * call read('classpath:common.feature')
    * reset() 
    * def defaultTestData = call read('classpath:default-test-data.js')
    * def keyPair = defaultTestData.personalAccounts['default-pa1']
    * def pa1 = defaultTestData.personalAccounts['default-pa1']
    * def defaultOrganizationId = defaultTestData.defaultOrganization.id;
    * def defaultProjectId = defaultTestData.defaultProject.id;
    * configure headers = call read('classpath:headers.js') { token: #(pa1.token)}

  Scenario: store supplychain with valid name should return a 201
    Given path '/api/nodes/'+defaultOrganizationId+'/projects'
    And request { name: 'project1', parentId: #(defaultOrganizationId)}
    When method POST
    Then status 201
    Then match response == {'id':'#uuid','name': 'project1', 'parentId':#(defaultOrganizationId), 'pathToRoot': [#(response.id), #(defaultOrganizationId)]}
    * def projectId = response.id
    Given path '/api/nodes/'+projectId+'/supplychains'
    And request { name: 'sc1', parentId: #(projectId)}
    When method POST
    Then status 201
    Then match response == {'id':'#uuid','name': 'sc1', 'parentId':#(projectId), 'pathToRoot': [#(response.id), #(projectId), #(defaultOrganizationId)]}
    * def scId = response.id
    Given path '/api/supplychains/'+scId
    When method GET
    Then status 200
    Then match response == {'id':'#uuid','name': 'sc1', 'parentId':#(projectId), 'pathToRoot': [#(response.id), #(projectId), #(defaultOrganizationId)]}
    Given path '/api/projects/'+projectId
    When method DELETE
    Then status 204
    Given path '/api/supplychains/'+scId
    When method GET
    Then status 404
    Then match response.messages[0].message == 'Resource with id ['+scId+'] not found'
    
  Scenario: store supplychain with non unique name for the parent should return a 400
    Given path '/api/nodes/'+defaultProjectId+'/supplychains'
    And request { name: 'sc1', parentId: #(defaultProjectId)}
    When method POST
    Then status 201
    Given path '/api/nodes/'+defaultProjectId+'/supplychains'
    And request { name: 'sc1', parentId: #(defaultProjectId)}
    When method POST
    Then status 400
    Then match response.messages[0].message == 'Supply Chain with name [sc1] already exists on project ['+defaultProjectId+']'
    
  Scenario: store sc with organization as parent should return a 400
    Given path '/api/nodes/'+defaultOrganizationId+'/supplychains'
    And request { name: 'sc1', parentId: #(defaultOrganizationId)}
    When method POST
    Then status 400
    Then match response.messages[0].message == 'invalid parent'
    
  Scenario: store sc on project on management node on organization should return a 201 delete management node
    Given path '/api/nodes/'+defaultOrganizationId+'/managementnodes'
    And request { name: 'node1', parentId: #(defaultOrganizationId)}
    When method POST
    Then status 201
    Then match response == {'id':'#uuid','name': 'node1', 'parentId':#(defaultOrganizationId), 'pathToRoot': [#(response.id), #(defaultOrganizationId)]}
    * def nodeId = response.id
    Given path '/api/nodes/'+response.id+'/projects'
    And request { name: 'project1', parentId: #(response.id)}
    When method POST
    Then status 201
    Then match response == {'id':'#uuid','name': 'project1', 'parentId':#(nodeId), 'pathToRoot': [#(response.id), #(nodeId), #(defaultOrganizationId)]}
    * def projectId = response.id
    Given path '/api/nodes/'+projectId+'/supplychains'
    And request { name: 'sc1', parentId: #(projectId)}
    When method POST
    Then status 201
    Then match response == {'id':'#uuid','name': 'sc1', 'parentId':#(projectId), 'pathToRoot': [#(response.id), #(projectId), #(nodeId), #(defaultOrganizationId)]}
    * def scId = response.id
    Given path '/api/managementnodes/'+nodeId
    When method DELETE
    Then status 204
    Given path '/api/supplychains/'+scId
    When method GET
    Then status 404
    Then match response.messages[0].message == 'Resource with id ['+scId+'] not found'
    
    
 