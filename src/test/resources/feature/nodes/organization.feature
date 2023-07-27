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
    * def defaultProjectId = defaultTestData.defaultProject.id;
    * configure headers = call read('classpath:headers.js') { token: #(pa1.token)}

  Scenario: store organization with valid name should return a 201
    * def extraAccount = paLogin('user6')
    * def accountId = karate.toString(extraAccount.personalAccount.id)
    * configure headers = call read('classpath:headers.js') { token: #(extraAccount.token)}
    * def result = call read('create-organization.feature') {org: { name: 'org1'}}
    * match result.response == { name: 'org1', id: '#uuid', 'pathToRoot':['#uuid'] }
    * match result.responseHeaders['Location'][0] contains '/api/organizations/'+result.response.id
    * def expectedPermissions = ['LINK_ADD','ROLE_WRITE','READ','RELEASE','WRITE']
    Given path '/api/roles/'+result.response.id
    When method GET
    Then status 200
    And match response[0] == {'id':'#uuid','resourceId':#(result.response.id),'identityId':#(accountId),'role':{'permissions':#(^^expectedPermissions)}}
    Given path '/api/organizations/'+result.response.id
    When method DELETE
    Then status 204
    Given path '/api/roles/'+result.response.id
    When method GET
    Then status 404
    Then match response.messages[0].message == 'Resource with id ['+result.response.id+'] not found'
    
  Scenario: get all authorized organizations
    * def extraAccount = paLogin('user6')
    * def accountId = karate.toString(extraAccount.personalAccount.id)
    * configure headers = call read('classpath:headers.js') { token: #(extraAccount.token)}
    * def result = call read('create-organization.feature') {org: { name: 'org1'}}
    Given path '/api/organizations'
    When method GET
    Then status 200
    And match response == [{ name: 'org1', id: '#uuid', 'pathToRoot':['#uuid'] }]
    
  Scenario: store organization with non unique name should return a 400
    * def extraAccount = paLogin('user6')
    * def accountId = karate.toString(extraAccount.personalAccount.id)
    * configure headers = call read('classpath:headers.js') { token: #(extraAccount.token)}
    * def result = call read('create-organization.feature') {org: { name: 'org1'}}
    * match result.response == { name: 'org1', id: '#uuid', 'pathToRoot':['#uuid'] }
    Given path '/api/organizations'
    And request { name: 'org1'}
    When method POST
    Then status 400
    And match response.messages[0].message == 'Organization with name [org1] already exists'
