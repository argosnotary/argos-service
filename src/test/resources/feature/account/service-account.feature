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
    * def defaultProjectId = defaultTestData.defaultProject.id;
    * configure headers = call read('classpath:headers.js') { token: #(pa1.token)}

  Scenario: store a service account with valid name should return a 201 and commit to audit log
    * def result = call read('create-service-account.feature') {projectId: #(defaultProjectId), sa: { name: 'sa1', projectId: #(defaultProjectId)}}
    * match result.response == { name: 'sa1', id: '#uuid', projectId: '#uuid', "providerSubject":"#uuid","inactiveKeyPairs":[] }
    * def auditlog = getAuditLogs()

  Scenario: delete service account should return a 204 and get should return a 403 and commit to audit log
    * def result = call read('create-service-account.feature') {projectId: #(defaultProjectId), sa: { name: 'sa1', projectId: #(defaultProjectId)}}
    * def restPath = '/api/projects/'+defaultProjectId+'/serviceaccounts/'+result.response.id
    Given path restPath
    When method DELETE
    Then status 204
    Given path restPath
    When method GET
    Then status 404

  Scenario: delete service account without WRITE permission should return a 403 error
    * def result = call read('create-service-account.feature') {projectId: #(defaultProjectId), sa: { name: 'sa1', projectId: #(defaultProjectId)}}
    * def restPath = '/api/projects/'+defaultProjectId+'/serviceaccounts/'+result.response.id
    * configure headers = call read('classpath:headers.js') { token: #(pa2.token)}
    Given path restPath
    When method DELETE
    Then status 403

  Scenario: store a service account without WRITE permission should return a 403 error
    * configure headers = call read('classpath:headers.js') { token: #(pa2.token)}
    Given path '/api/projects/'+defaultProjectId+'/serviceaccounts'
    And request { name: 'sa1', projectId: #(defaultProjectId)}
    When method POST
    Then status 403

  Scenario: store a service account without authentication should return a 401 error
    * configure headers = null
    Given path '/api/projects/'+defaultProjectId+'/serviceaccounts'
    And request { name: 'sa1', projectId: #(defaultProjectId)}
    When method POST
    Then status 401

  Scenario: store a service account with a non existing parent label id should return a 403
    Given path '/api/projects/940935f6-22bc-4d65-8c5b-a0599dedb510/serviceaccounts'
    And request { name: 'sa1', projectId: '940935f6-22bc-4d65-8c5b-a0599dedb510'}
    When method POST
    Then status 404
    Then match response.message == 'Resource with id [940935f6-22bc-4d65-8c5b-a0599dedb510] not found'

  Scenario: store two service accounts with the same name should return a 400
    * call read('create-service-account.feature') {projectId: #(defaultProjectId), sa: { name: 'sa1', projectId: #(defaultProjectId)}}
    Given path '/api/projects/'+defaultProjectId+'/serviceaccounts'
    And request { name: 'sa1', projectId: #(defaultProjectId)}
    When method POST
    Then status 400
    And match response.messages[0].message contains 'Service account already exists with projectId ['+defaultProjectId+'] and name [sa1]'
    
  Scenario: retrieve service account should return a 200
    * def result = call read('create-service-account.feature') {projectId: #(defaultProjectId), sa: { name: 'sa1', projectId: #(defaultProjectId)}}
    * def restPath = '/api/projects/'+defaultProjectId+'/serviceaccounts/'+result.response.id
    Given path restPath
    When method GET
    Then status 200
    And match response == { name: 'sa1', id: '#(result.response.id)', projectId: #(defaultProjectId), "providerSubject":"#uuid", "inactiveKeyPairs":[] }

  Scenario: retrieve service account without READ permission should return a 403 error
    * def result = call read('create-service-account.feature') {projectId: #(defaultProjectId), sa: { name: 'sa1', projectId: #(defaultProjectId)}}
    * configure headers = call read('classpath:headers.js') { token: #(pa4.token)}
    * def restPath = '/api/projects/'+defaultProjectId+'/serviceaccounts/'+result.response.id
    Given path restPath
    When method GET
    Then status 403

  Scenario: retrieve service account with implicit READ permission should return a 200 error
    * configure headers = call read('classpath:headers.js') { token: #(pa3.token)}
    * def restPath = '/api/projects/'+defaultProjectId+'/serviceaccounts/'+sa1.serviceAccount.id
    Given path restPath
    When method GET
    Then status 200
    And match response == { name: 'default-sa1', projectId: #(defaultProjectId), providerSubject:"#uuid", id:"#uuid", activeKeyPair: "#ignore", "inactiveKeyPairs":[]} 

  Scenario: create a service account key should return a 200 and commit to audit log
    * def result = call read('create-service-account.feature') {projectId: #(defaultProjectId), sa: { name: 'sa1', projectId: #(defaultProjectId)}}
    * def keyPair = read('classpath:testmessages/key/sa-keypair1.json')
    * def result = call read('create-service-account-key.feature') {projectId: #(defaultProjectId), accountId: #(result.response.id), key: #(keyPair)}
    * match result.response == {keyId: #(keyPair.keyId), publicKey: #(keyPair.publicKey), encryptedPrivateKey: #(keyPair.encryptedPrivateKey)}

  Scenario: create a service account key without WRITE permission should return a 403 error
    * def result = call read('create-service-account.feature') {projectId: #(defaultProjectId), sa: { name: 'sa1', projectId: #(defaultProjectId)}}
    * def keyPair = read('classpath:testmessages/key/sa-keypair1.json')
    * configure headers = call read('classpath:headers.js') { token: #(pa2.token)}
    Given path '/api/projects/'+defaultProjectId+'/serviceaccounts/'+result.response.id+'/key'
    And request keyPair
    When method POST
    Then status 403

  Scenario: create a service account key without authentication should return a 401 error
    * def result = call read('create-service-account.feature') {projectId: #(defaultProjectId), sa: { name: 'sa1', projectId: #(defaultProjectId)}}
    * def keyPair = read('classpath:testmessages/key/sa-keypair1.json')
    * configure headers = null
    Given path '/api/projects/'+defaultProjectId+'/serviceaccounts/'+result.response.id+'/key'
    And request keyPair
    When method POST
    Then status 401

  Scenario: get a active service account key should return a 200
    * def result = call read('create-service-account.feature') {projectId: #(defaultProjectId), sa: { name: 'sa1', projectId: #(defaultProjectId)}}
    * def keyPair = read('classpath:testmessages/key/sa-keypair1.json')
    * call read('create-service-account-key.feature') {projectId: #(defaultProjectId), accountId: #(result.response.id), key: #(keyPair)}
    * def restPath = '/api/projects/'+defaultProjectId+'/serviceaccounts/'+result.response.id+'/key'
    Given path restPath
    When method GET
    Then status 200
    And match response == {keyId: #(keyPair.keyId), publicKey: #(keyPair.publicKey), encryptedPrivateKey: #(keyPair.encryptedPrivateKey)}

  Scenario: get a active service account key with implicit read permission should return a 200
    * configure headers = call read('classpath:headers.js') { token: #(pa3.token)}
    * def result = call read('create-service-account.feature') {projectId: #(defaultProjectId), sa: { name: 'sa1', projectId: #(defaultProjectId)}}
    * def keyPair = read('classpath:testmessages/key/sa-keypair1.json')
    * call read('create-service-account-key.feature') {projectId: #(defaultProjectId), accountId: #(result.response.id), key: #(keyPair)}
    * def restPath = '/api/projects/'+defaultProjectId+'/serviceaccounts/'+result.response.id+'/key'
    Given path restPath
    When method GET
    Then status 200
    And match response == {keyId: #(keyPair.keyId), publicKey: #(keyPair.publicKey), encryptedPrivateKey: #(keyPair.encryptedPrivateKey)}

  Scenario: get a active service account key without READ permission should return a 403 error
    * def result = call read('create-service-account.feature') {projectId: #(defaultProjectId), sa: { name: 'sa1', projectId: #(defaultProjectId)}}
    * def keyPair = read('classpath:testmessages/key/sa-keypair1.json')
    * call read('create-service-account-key.feature') {projectId: #(defaultProjectId), accountId: #(result.response.id), key: #(keyPair)}
    * configure headers = call read('classpath:headers.js') { token: #(pa4.token)}
    * def restPath = '/api/projects/'+defaultProjectId+'/serviceaccounts/'+result.response.id+'/key'
    Given path restPath
    When method GET
    Then status 403

  Scenario: get a active service account key without authorization should return a 401 error
    * def result = call read('create-service-account.feature') {projectId: #(defaultProjectId), sa: { name: 'sa1', projectId: #(defaultProjectId)}}
    * def keyPair = read('classpath:testmessages/key/sa-keypair1.json')
    * call read('create-service-account-key.feature') {projectId: #(defaultProjectId), accountId: #(result.response.id), key: #(keyPair)}
    * def restPath = '/api/projects/'+defaultProjectId+'/serviceaccounts/'+result.response.id+'/key'
    * configure headers = null
    Given path restPath
    When method GET
    Then status 401

  Scenario: get active key of authenticated sa should return a 200
    * configure headers =  call read('classpath:headers.js') { token: #(sa1.token)}
    Given path '/api/serviceaccounts/me/activekey'
    When method GET
    Then status 200
    And match response == {keyId: #(sa1.serviceAccount.activeKeyPair.keyId), publicKey: #(sa1.serviceAccount.activeKeyPair.publicKey), encryptedPrivateKey: #(sa1.serviceAccount.activeKeyPair.encryptedPrivateKey)}

  Scenario: get active key of authenticated sa with invalid credentials should return a 401
    * configure headers =  call read('classpath:headers.js') { token: "bar"}
    Given path '/api/serviceaccounts/me/activekey'
    When method GET
    Then status 401

  Scenario: get an active service account key after update should return a 200
    * def result = call read('create-service-account.feature') {projectId: #(defaultProjectId), sa: { name: 'sa1', projectId: #(defaultProjectId)}}
    * def keyPair = read('classpath:testmessages/key/sa-keypair1.json')
    * call read('create-service-account-key.feature') {projectId: #(defaultProjectId), accountId: #(result.response.id), key: #(keyPair)}
    * def restPath = '/api/projects/'+defaultProjectId+'/serviceaccounts/'+result.response.id+'/key'
    Given path restPath
    When method GET
    Then status 200
    And match response == {keyId: #(keyPair.keyId), publicKey: #(keyPair.publicKey), encryptedPrivateKey: #(keyPair.encryptedPrivateKey)}



