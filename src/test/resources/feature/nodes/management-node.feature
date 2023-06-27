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

Feature: ManagementNode

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
    * configure headers = call read('classpath:headers.js') { token: #(pa1.token)}

  Scenario: store management node with valid name should return a 201
    Given path '/api/nodes/'+defaultOrganizationId+'/managementnodes'
    And request { name: 'node1', parentId: #(defaultOrganizationId)}
    When method POST
    Then status 201
    Then match response == {'id':'#uuid','name': 'node1', 'parentId':#(defaultOrganizationId), 'pathToRoot': [#(response.id), #(defaultOrganizationId)]}
    Given path '/api/managementnodes/'+response.id
    When method DELETE
    Then status 204
    
  Scenario: store management node with non unique name for the parent should return a 400
    Given path '/api/nodes/'+defaultOrganizationId+'/managementnodes'
    And request { name: 'node1', parentId: #(defaultOrganizationId)}
    When method POST
    Then status 201
    Given path '/api/nodes/'+defaultOrganizationId+'/managementnodes'
    And request { name: 'node1', parentId: #(defaultOrganizationId)}
    When method POST
    Then status 400
    Then match response.messages[0].message == 'Management Node with name [node1] already exists on parent ['+defaultOrganizationId+']'
    
  Scenario: store management node with project as parent should return a 400
    Given path '/api/nodes/'+defaultProjectId+'/managementnodes'
    And request { name: 'node1', parentId: #(defaultProjectId)}
    When method POST
    Then status 400
    Then match response.messages[0].message == 'invalid parent'
    
 