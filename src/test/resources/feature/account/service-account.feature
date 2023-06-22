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

Feature: Non Personal Account

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
    * def pa5 = defaultTestData.personalAccounts['default-pa5']
    * def sa1 = defaultTestData.serviceAccounts['default-sa1']
    * def defaultProjectId = defaultTestData.defaultRootLabel.id;
    * configure headers = call read('classpath:headers.js') { token: #(pa1.token)}

  Scenario: store a service account with valid name should return a 201 and commit to audit log
    * def result = call read('create-service-account.feature') { name: 'sa1', parentLabelId: #(rootLabelId)}
    * match result.response == { userName: 'sa1', id: '#uuid', parentLabelId: '#uuid', "providerSubject":"#uuid","inactiveKeyPairs":[] }
    * def auditlog = getAuditLogs()
    And match auditlog contains 'createServiceAccount'
    And match auditlog contains 'createServiceAccount'


  Scenario: delete service account should return a 200 and get should return a 403 and commit to audit log
    * def result = call read('create-service-account.feature') { userName: 'sa1', parentLabelId: #(rootLabelId)}
    * def restPath = '/api/serviceaccount/'+result.response.id
    Given path restPath
    When method DELETE
    Then status 204
    Given path restPath
    When method GET
    Then status 403
    * def auditlog = getAuditLogs()
    And match auditlog contains 'deleteServiceAccount'
    And match auditlog contains 'serviceAccountId'

  Scenario: delete service account without TREE_EDIT permission should return a 403 error
    * def result = call read('create-service-account.feature') { userName: 'sa1', parentLabelId: #(rootLabelId)}
    * def restPath = '/api/serviceaccount/'+result.response.id
    * configure headers = call read('classpath:headers.js') { token: #(pa2.token)}
    Given path restPath
    When method DELETE
    Then status 403

  Scenario: store a service account without TREE_EDIT permission should return a 403 error
    * configure headers = call read('classpath:headers.js') { token: #(pa2.token)}
    Given path '/api/serviceaccount'
    And request { userName: 'sa1', parentLabelId: #(rootLabelId)}
    When method POST
    Then status 403

  Scenario: store a service account without authorization should return a 401 error
    * configure headers = null
    Given path '/api/serviceaccount'
    And request { userName: 'sa1', parentLabelId: #(rootLabelId)}
    When method POST
    Then status 401

  Scenario: store a service account with a non existing parent label id should return a 403
    Given path '/api/serviceaccount'
    And request { userName: 'label', parentLabelId: '940935f6-22bc-4d65-8c5b-a0599dedb510'}
    When method POST
    Then status 403
    And match response.message == 'Access denied'

  Scenario: store two service accounts with the same userName should return a 400
    * call read('create-service-account.feature') { userName: 'sa1', parentLabelId: #(rootLabelId)}
    Given path '/api/serviceaccount'
    And request { userName: 'sa1', parentLabelId: #(rootLabelId)}
    When method POST
    Then status 400
    And match response.messages[0].message contains "service account with name: sa1 and parentLabelId:"

  Scenario: retrieve service account should return a 200
    * def result = call read('create-service-account.feature') { userName: 'sa1', parentLabelId: #(rootLabelId)}
    * def restPath = '/api/serviceaccount/'+result.response.id
    Given path restPath
    When method GET
    Then status 200
    And match response == { userName: 'sa1', id: '#(result.response.id)', parentLabelId: #(rootLabelId), "providerSubject":"#uuid","inactiveKeyPairs":[] }

  Scenario: retrieve service account without READ permission should return a 403 error
    * def result = call read('create-service-account.feature') { userName: 'sa1', parentLabelId: #(rootLabelId)}
    * configure headers = call read('classpath:headers.js') { token: #(pa4.token)}
    * def restPath = '/api/serviceaccount/'+result.response.id
    Given path restPath
    When method GET
    Then status 403

  Scenario: retrieve service account with implicit READ permission should return a 200 error
    * configure headers = call read('classpath:headers.js') { token: #(pa3.token)}
    * def restPath = '/api/serviceaccount/'+sa1.serviceAccount.id
    Given path restPath
    When method GET
    Then status 200
    And match response == { userName: 'default-sa1', parentLabelId: #(rootLabelId), providerSubject:"#uuid", id:"#uuid", activeKeyPair: "#ignore", "inactiveKeyPairs":[]} 

  Scenario: update a service account should return a 200 and commit to audit log
    * def createResult = call read('create-service-account.feature') { userName: 'sa1', parentLabelId: #(rootLabelId)}
    * def accountId = createResult.response.id
    * def restPath = '/api/serviceaccount/'+accountId
    Given path restPath
    And request { userName: 'sa2', parentLabelId: #(rootLabelId)}
    When method PUT
    Then status 200
    And match response == { userName: 'sa2', id: '#(accountId)', parentLabelId: #(rootLabelId), providerSubject:"#uuid","inactiveKeyPairs":[]}
    * def auditlog = getAuditLogs()
    And match auditlog contains 'updateServiceAccountById'
    And match auditlog contains 'serviceAccountId'
    And match auditlog contains 'serviceAccount'

  Scenario: update a service account without TREE_EDIT permission should return a 403 error
    * def createResult = call read('create-service-account.feature') { userName: 'sa1', parentLabelId: #(rootLabelId)}
    * configure headers = call read('classpath:headers.js') { token: #(pa2.token)}
    * def accountId = createResult.response.id
    * def restPath = '/api/serviceaccount/'+accountId
    Given path restPath
    And request { userName: 'sa2', parentLabelId: #(rootLabelId)}
    When method PUT
    Then status 403

  Scenario: create a service account key should return a 200 and commit to audit log
    * def createResult = call read('create-service-account.feature') { userName: 'sa1', parentLabelId: #(rootLabelId)}
    * def accountId = createResult.response.id
    * def keyPair = read('classpath:testmessages/key/sa-keypair1.json')
    * def result = call read('create-service-account-key.feature') {accountId: #(accountId), key: #(keyPair)}
    * match result.response == {keyId: #(keyPair.keyId), publicKey: #(keyPair.publicKey), encryptedPrivateKey: #(keyPair.encryptedPrivateKey)}
    * def auditlog = getAuditLogs()
    And match auditlog contains 'createServiceAccountKeyById'
    And match auditlog contains 'serviceAccountId'
    And match auditlog contains 'keyPair'

  Scenario: create a service account key without TREE_EDIT permission should return a 403 error
    * def createResult = call read('create-service-account.feature') { userName: 'sa1', parentLabelId: #(rootLabelId)}
    * def accountId = createResult.response.id
    * def keyPair = read('classpath:testmessages/key/sa-keypair1.json')
    * configure headers = call read('classpath:headers.js') { token: #(pa2.token)}
    Given path '/api/serviceaccount/'+accountId+'/key'
    And request keyPair
    When method POST
    Then status 403

  Scenario: create a service account key without authorization should return a 401 error
    * def createResult = call read('create-service-account.feature') { userName: 'sa1', parentLabelId: #(rootLabelId)}
    * def accountId = createResult.response.id
    * def keyPair = read('classpath:testmessages/key/sa-keypair1.json')
    * configure headers = null
    Given path '/api/serviceaccount/'+accountId+'/key'
    And request keyPair
    When method POST
    Then status 401

  Scenario: get a active service account key should return a 200
    * def createResult = call read('create-service-account.feature') { userName: 'sa1', parentLabelId: #(rootLabelId)}
    * def accountId = createResult.response.id
    * def keyPair = read('classpath:testmessages/key/sa-keypair1.json')
    * call read('create-service-account-key.feature') {accountId: #(accountId), key: #(keyPair)}
    * def restPath = '/api/serviceaccount/'+accountId+'/key'
    Given path restPath
    When method GET
    Then status 200
    And match response == {keyId: #(keyPair.keyId), publicKey: #(keyPair.publicKey), encryptedPrivateKey: #(keyPair.encryptedPrivateKey)}

  Scenario: get a active service account key with implicit read permission should return a 200

    * configure headers = call read('classpath:headers.js') { token: #(pa3.token)}
    * def result = call read('create-service-account.feature') { userName: 'sa1', parentLabelId: #(rootLabelId)}
    * def accountId = result.response.id
    * def keyPair = read('classpath:testmessages/key/sa-keypair1.json')
    * call read('create-service-account-key.feature') {accountId: #(accountId), key: #(keyPair)}
    * def restPath = '/api/serviceaccount/'+result.response.id+'/key'
    Given path restPath
    When method GET
    Then status 200
    And match response == {keyId: #(keyPair.keyId), publicKey: #(keyPair.publicKey), encryptedPrivateKey: #(keyPair.encryptedPrivateKey)}

  Scenario: get a active service account key without READ permission should return a 403 error
    * def createResult = call read('create-service-account.feature') { userName: 'sa1', parentLabelId: #(rootLabelId)}
    * def accountId = createResult.response.id
    * def keyPair = read('classpath:testmessages/key/sa-keypair1.json')
    * call read('create-service-account-key.feature') {accountId: #(accountId), key: #(keyPair)}
    * configure headers = call read('classpath:headers.js') { token: #(pa4.token)}
    * def restPath = '/api/serviceaccount/'+accountId+'/key'
    Given path restPath
    When method GET
    Then status 403

  Scenario: get a active service account key without authorization should return a 401 error
    * def createResult = call read('create-service-account.feature') { userName: 'sa1', parentLabelId: #(rootLabelId)}
    * def accountId = createResult.response.id
    * def keyPair = read('classpath:testmessages/key/sa-keypair1.json')
    * call read('create-service-account-key.feature') {accountId: #(accountId), key: #(keyPair)}
    * def restPath = '/api/serviceaccount/'+accountId+'/key'
    * configure headers = null
    Given path restPath
    When method GET
    Then status 401

  Scenario: get active key of authenticated sa should return a 200
    * configure headers =  call read('classpath:headers.js') { token: #(sa1.token)}
    Given path '/api/serviceaccount/me/activekey'
    When method GET
    Then status 200
    And match response == {keyId: #(sa1.serviceAccount.activeKeyPair.keyId), publicKey: #(sa1.serviceAccount.activeKeyPair.publicKey), encryptedPrivateKey: #(sa1.serviceAccount.activeKeyPair.encryptedPrivateKey)}

  Scenario: get active key of authenticated sa with invalid credentials should return a 401
    * configure headers =  call read('classpath:headers.js') { token: "bar"}
    Given path '/api/serviceaccount/me/activekey'
    When method GET
    Then status 401

  Scenario: get an active service account key after update should return a 200
    * def createResult = call read('create-service-account.feature') { userName: 'sa1', parentLabelId: #(rootLabelId)}
    * def accountId = createResult.response.id
    * def keyPair = read('classpath:testmessages/key/sa-keypair1.json')
    * call read('create-service-account-key.feature') {accountId: #(accountId), key: #(keyPair)}
    * def restPathKey = '/api/serviceaccount/'+accountId+'/key'
    * def restPathUpdate = '/api/serviceaccount/'+ accountId
    Given path restPathUpdate
    And request { userName: 'sa2', parentLabelId: #(rootLabelId)}
    When method PUT
    Then status 200
    Given path restPathKey
    When method GET
    Then status 200
    And match response == {keyId: #(keyPair.keyId), publicKey: #(keyPair.publicKey), encryptedPrivateKey: #(keyPair.encryptedPrivateKey)}

