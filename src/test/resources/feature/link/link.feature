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

Feature: Link

  Background:
    * url karate.properties['server.baseurl']
    * call read('classpath:common.feature')
    * reset()
    * def defaultTestData = call read('classpath:default-test-data.js')
    * def pa1 = defaultTestData.personalAccounts['default-pa1']
    * def pa2 = defaultTestData.personalAccounts['default-pa2']
    * def pa3 = defaultTestData.personalAccounts['default-pa3']
    * def pa4 = defaultTestData.personalAccounts['default-pa4']
    * def pa5 = defaultTestData.personalAccounts['default-pa5']
    * def sa1 = defaultTestData.serviceAccounts['default-sa1']
    * def defaultOrganizationId = defaultTestData.defaultOrganization.id;
    * def defaultProjectId = defaultTestData.defaultProject.id;
    * configure headers = call read('classpath:headers.js') { token: #(defaultTestData.ownerToken)}
    Given path '/api/nodes/'+defaultProjectId+'/supplychains'
    And request { name: 'sc1', parentId: #(defaultProjectId)}
    When method POST
    Then status 201
    Then match response == {'id':'#uuid','name': 'sc1', 'parentId':#(defaultProjectId), 'pathToRoot': [#(response.id), #(defaultProjectId), #(defaultOrganizationId)]}
    * def scId = response.id
    * def linkPath = '/api/supplychains/'+ scId + '/link'
    * def validLink = 'classpath:testmessages/link/valid-link.json'
    * def validLinkResponse = read('classpath:testmessages/link/valid-link-response.json')
    * def linkToBesigned = read(validLink)

  Scenario: store link with valid specifications should return a 204 and commit to audit log
    * def linkResponse = call read('create-link.feature') {supplyChainId:#(scId), linkFile:#(validLink), signingAccount:#(sa1)}
    * match linkResponse.response == validLinkResponse[0]
    Given path linkPath
    When method GET
    Then status 200
    And match response == validLinkResponse
    
  Scenario: SERVICE_ACCOUNT can store a link with valid specifications and should return a 204
    Given path '/api/nodes/'+defaultProjectId+'/supplychains'
    And request { name: 'other', parentId: #(defaultProjectId)}
    When method POST
    Then status 201
    * call read('create-link.feature') {supplyChainId:#(response.id), linkFile:#(validLink), signingAccount:#(sa1)}

  Scenario: user with local permission LINK_ADD can store a link
    Given path '/api/nodes/'+defaultProjectId+'/supplychains'
    And request { name: 'other', parentId: #(defaultProjectId)}
    When method POST
    Then status 201
    * def signedLink = call signLink { passphrase: #(sa1.passphrase), keyPair: #(sa1.serviceAccount.activeKeyPair), linkMetaBlock: #(linkToBesigned)}
    * configure headers = call read('classpath:headers.js') { token: #(pa5.token)}
    Given path '/api/supplychains/'+ response.id + '/link'
    And request signedLink
    When method POST
    Then status 201

  Scenario: user without local permission LINK_ADD cannot store a link
    Given path '/api/nodes/'+defaultProjectId+'/supplychains'
    And request { name: 'other', parentId: #(defaultProjectId)}
    When method POST
    Then status 201
    * def signedLink = call signLink { passphrase: #(sa1.passphrase), keyPair: #(sa1.serviceAccount.activeKeyPair), linkMetaBlock: #(linkToBesigned)}
    * configure headers = call read('classpath:headers.js') { token: #(pa2.token)}
    Given path '/api/supplychains/'+ response.id + '/link'
    And request signedLink
    When method POST
    Then status 403

  Scenario: SERVICE_ACCOUNT on other project cannot store a link
    Given path '/api/nodes/'+defaultOrganizationId+'/projects'
    And request { name: 'project1', parentId: #(defaultOrganizationId)}
    When method POST
    Then status 201
    * def projectId = response.id
    Given path '/api/nodes/'+projectId+'/supplychains'
    And request { name: 'other', parentId: #(projectId)}
    When method POST
    Then status 201
    * def signedLink = call signLink { passphrase: #(sa1.passphrase), keyPair: #(sa1.serviceAccount.activeKeyPair), linkMetaBlock: #(linkToBesigned)}
    * configure headers = call read('classpath:headers.js') { token: #(sa1.token)}
    Given path '/api/supplychains/'+ response.id + '/link'
    And request signedLink
    When method POST
    Then status 403

  Scenario: store link with invalid specifications should return a 400 error
    Given path linkPath
    And request read('classpath:testmessages/link/invalid-link.json')
    When method POST
    Then status 400
    And match response contains read('classpath:testmessages/link/invalid-link-response.json')

  Scenario: store link without authorization should return a 401 error
    * configure headers = null
    Given path linkPath
    And request validLink
    And header Content-Type = 'application/json'
    When method POST
    Then status 401

  Scenario: find link with valid supplychainid should return a 200
    * call read('create-link.feature') {supplyChainId:#(scId), linkFile:#(validLink), signingAccount:#(sa1)}
    * configure headers = call read('classpath:headers.js') { token: #(defaultTestData.ownerToken)}
    Given path linkPath
    When method GET
    Then status 200
    And match response == validLinkResponse

  Scenario: find link without authorization should return a 401 error
    * call read('create-link.feature') {supplyChainId:#(supplyChain.response.id), linkFile:#(validLink), signingAccount:#(sa1)}
    * configure headers = null
    Given path linkPath
    And header Content-Type = 'application/json'
    When method GET
    Then status 401

  Scenario: find link with valid supplychainid and optionalHash should return a 200
    * call read('create-link.feature') {supplyChainId:#(scId), linkFile:#(validLink), signingAccount:#(sa1)}
    * configure headers = call read('classpath:headers.js') { token: #(defaultTestData.ownerToken)}
    Given path linkPath
    And param hash = '61a0af2b177f02a14bab478e68d4907cda4dc3f642ade0432da8350ca199302b'
    When method GET
    Then status 200
    And match response == validLinkResponse
    
  Scenario: find link with valid supplychainid and invalid optionalHash should return a 200
    * call read('create-link.feature') {supplyChainId:#(scId), linkFile:#(validLink), signingAccount:#(sa1)}
    * configure headers = call read('classpath:headers.js') { token: #(defaultTestData.ownerToken)}
    Given path linkPath
    And param hash = '0123456789012345678901234567890123456789012345678901234567890123'
    When method GET
    Then status 200
    And match response == []

  Scenario: user with READ local permission can find link with valid supplychainid and optionalHash should return a 200
    * call read('create-link.feature') {supplyChainId:#(scId), linkFile:#(validLink), signingAccount:#(sa1)}
    * configure headers = call read('classpath:headers.js') { token: #(pa2.token)}
    Given path linkPath
    And param hash = '61a0af2b177f02a14bab478e68d4907cda4dc3f642ade0432da8350ca199302b'
    When method GET
    Then status 200
    And match response == validLinkResponse
    #And match response[*] contains read('classpath:testmessages/link/valid-link-response.json')

  Scenario: user without READ local permission cannot find link with valid supplychainid and optionalHash should return a 403
    * call read('create-link.feature') {supplyChainId:#(scId), linkFile:#(validLink), signingAccount:#(sa1)}
    * configure headers = call read('classpath:headers.js') { token: #(pa4.token)}
    Given path linkPath
    And param hash = '61a0af2b177f02a14bab478e68d4907cda4dc3f642ade0432da8350ca199302b'
    When method GET
    Then status 403


