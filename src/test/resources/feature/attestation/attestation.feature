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
    * def attestPath = '/api/supplychains/'+ scId + '/attest'
    * def validAttest = 'classpath:testmessages/attestation/valid-attest.json'
    * def validAttestResponse = read('classpath:testmessages/attestation/valid-attest-response.json')

  Scenario: store attestation with valid specifications should return a 201
    * def keyId = sa1.serviceAccount.activeKeyPair.keyId
    * def attestResponse = call read('create-attest.feature') {supplyChainId:#(scId), attestFile:#(validAttest), signingAccount:#(sa1)}
    * match attestResponse.response == validAttestResponse
    Given path attestPath
    When method GET
    Then status 200
    And match response == [#(validAttestResponse)]
    
  Scenario: SERVICE_ACCOUNT can store a attestation with valid specifications and should return a 204
    Given path '/api/nodes/'+defaultProjectId+'/supplychains'
    And request { name: 'other', parentId: #(defaultProjectId)}
    When method POST
    Then status 201
    * call read('create-attest.feature') {supplyChainId:#(response.id), attestFile:#(validAttest), signingAccount:#(sa1)}
    
  Scenario: user with local permission ATTESTATION_ADD can store a attestation
    Given path '/api/nodes/'+defaultProjectId+'/supplychains'
    And request { name: 'other', parentId: #(defaultProjectId)}
    When method POST
    Then status 201
    * def supplyChainId = response.id
    * def attestToBesigned = read(validAttest)
    * def signedAttest = call signAttestation { passphrase: #(sa1.passphrase), keyPair: #(sa1.serviceAccount.activeKeyPair), attestation: #(attestToBesigned)}
    * configure headers = call read('classpath:headers.js') { token: #(pa5.token)}
    Given path '/api/supplychains/'+ supplyChainId + '/attest'
    And request signedAttest
    When method POST
    Then status 201
    
  Scenario: user without local permission ATTESTATION_ADD cannot store a attestation
    Given path '/api/nodes/'+defaultProjectId+'/supplychains'
    And request { name: 'other', parentId: #(defaultProjectId)}
    When method POST
    Then status 201
    * def supplyChainId = response.id
    * def attestToBesigned = read(validAttest)
    * def signedAttest = call signAttestation { passphrase: #(sa1.passphrase), keyPair: #(sa1.serviceAccount.activeKeyPair), attestation: #(attestToBesigned)}
    * configure headers = call read('classpath:headers.js') { token: #(pa2.token)}
    Given path '/api/supplychains/'+ supplyChainId + '/attest'
    And request signedAttest
    When method POST
    Then status 403

  Scenario: SERVICE_ACCOUNT on other project cannot store a attestation
    Given path '/api/nodes/'+defaultOrganizationId+'/projects'
    And request { name: 'project1', parentId: #(defaultOrganizationId)}
    When method POST
    Then status 201
    * def projectId = response.id
    Given path '/api/nodes/'+projectId+'/supplychains'
    And request { name: 'other', parentId: #(projectId)}
    When method POST
    Then status 201
    * def supplyChainId = response.id
    * def attestToBesigned = read(validAttest)
    * def signedAttest = call signAttestation { passphrase: #(sa1.passphrase), keyPair: #(sa1.serviceAccount.activeKeyPair), attestation: #(attestToBesigned)}
    * configure headers = call read('classpath:headers.js') { token: #(sa1.token)}
    Given path '/api/supplychains/'+ supplyChainId + '/attest'
    And request signedAttest
    When method POST
    Then status 403

  Scenario: store attestation with invalid specifications should return a 400 error
    * def supplyChainId = scId
    Given path attestPath
    And request read('classpath:testmessages/attestation/invalid-attest.json')
    When method POST
    Then status 400
    And match response contains read('classpath:testmessages/attestation/invalid-attest-response.json')

  Scenario: store attestation without authorization should return a 403 error
    * configure headers = null
    Given path attestPath
    And request validAttest
    And header Content-Type = 'application/json'
    When method POST
    Then status 403
  
  Scenario: find attestation without authorization should return a 401 error
    * call read('create-attest.feature') {supplyChainId:#(scId), attestFile:#(validAttest), signingAccount:#(sa1)}
    * configure headers = null
    Given path attestPath
    And header Content-Type = 'application/json'
    When method GET
    Then status 401
  
  Scenario: find attestation with valid supplychainid and optionalHash should return a 200
    * def keyId = sa1.serviceAccount.activeKeyPair.keyId
    * def attestResponse = call read('create-attest.feature') {supplyChainId:#(scId), attestFile:#(validAttest), signingAccount:#(sa1)}
    * configure headers = call read('classpath:headers.js') { token: #(defaultTestData.ownerToken)}
    Given path attestPath
    And param hash = '956c0ea99f13ef0c866c87f1afd457d798198e96ad0561dee7d59d4e95444484'
    When method GET
    Then status 200
    And match response == [#(validAttestResponse)]
    
  Scenario: find attestation with valid supplychainid and invalid optionalHash should return a 200
    * def attestResponse = call read('create-attest.feature') {supplyChainId:#(scId), attestFile:#(validAttest), signingAccount:#(sa1)}
    * configure headers = call read('classpath:headers.js') { token: #(defaultTestData.ownerToken)}
    Given path attestPath
    And param hash = '0123456789012345678901234567890123456789012345678901234567890123'
    When method GET
    Then status 200
    And match response == []
    
  Scenario: user with READ local permission can find attestation with valid supplychainid and optionalHash should return a 200
    * def keyId = sa1.serviceAccount.activeKeyPair.keyId
    * call read('create-attest.feature') {supplyChainId:#(scId), attestFile:#(validAttest), signingAccount:#(sa1)}
    * configure headers = call read('classpath:headers.js') { token: #(pa2.token)}
    Given path attestPath
    And param hash = '956c0ea99f13ef0c866c87f1afd457d798198e96ad0561dee7d59d4e95444484'
    When method GET
    Then status 200
    And match response == [#(validAttestResponse)]

  Scenario: user without READ local permission cannot find attestation with valid supplychainid and optionalHash should return a 403
    * call read('create-attest.feature') {supplyChainId:#(scId), attestFile:#(validAttest), signingAccount:#(sa1)}
    * configure headers = call read('classpath:headers.js') { token: #(pa4.token)}
    Given path attestPath
    And param hash = '61a0af2b177f02a14bab478e68d4907cda4dc3f642ade0432da8350ca199302b'
    When method GET
    Then status 403

    
    
    
  