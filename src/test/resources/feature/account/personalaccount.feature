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

Feature: Personal Account

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
    * configure headers = call read('classpath:headers.js') { token: #(defaultTestData.ownerToken)}

  Scenario: get Personal Account profile should return 200
    * def extraAccount = paLogin('user6')
    * def accountId = karate.toString(extraAccount.personalAccount.id)
    * def expectedResponse = read('classpath:testmessages/personal-account/extra-account-response.json')
    * configure headers = call read('classpath:headers.js') { token: #(extraAccount.token)}
    Given path '/api/personalaccounts/me'
    When method GET
    Then status 200
    Then match response == expectedResponse
    
  Scenario: createKey should return 204 and commit to audit log
    * def keyPair = read('classpath:testmessages/key/personal-keypair.json')
    * def extraAccount = paLogin('user6')
    * configure headers = call read('classpath:headers.js') { token: #(extraAccount.token)}
    Given path '/api/personalaccounts/me/key'
    And request read('classpath:testmessages/key/personal-keypair.json')
    When method POST
    Then status 204
    #* def auditlog = getAuditLogs()
    # And match auditlog contains 'createKey'
    # And match auditlog contains 'keyPair'
    Given path '/api/personalaccounts/me/key'
    And request keyPair
    When method POST
    Then status 204
    Given path '/api/personalaccounts/me/key'
    When method GET
    Then status 200
    Then match response == keyPair

  Scenario: createKey with invalid key should return 400
    * def extraAccount = paLogin('user6')
    * configure headers = call read('classpath:headers.js') { token: #(extraAccount.token)}
    Given path '/api/personalaccounts/me/key'
    And request {"keyId": "invalidkeyid","publicKey": "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC/Ldm84IhBvssdweZOZSPcx87J0Xy63g0JhlOYlr66aKmbXz5YD+J+b4NlIIbvaa5sEg4FS0+gkOPgexqCzgRUqHK5coLchpuLFggmDiL4ShqGIvqb/HPq7Aauk8Ss+0TaHfkJjd2kEBPRgWLII1gytjKkqlRGD/LxRtsppnleQwIDAQAB","encryptedPrivateKey": null}
    When method POST
    Then status 400

  Scenario: get account by id should return a 200
    * def expectedResponse = read('classpath:testmessages/personal-account/get-account-by-id-response.json')
    * def extraAccount = paLogin('user6')
    Given path '/api/personalaccounts/'+pa1.personalAccount.id
    When method GET
    Then status 200
    Then match response == expectedResponse
    
  Scenario: get account by id should return a 200
    Given path '/api/personalaccounts/'+pa1.personalAccount.id+'/key'
    When method GET
    Then status 200
    Then match response == {keyId: #(pa1.personalAccount.activeKeyPair.keyId), publicKey: #(pa1.personalAccount.activeKeyPair.publicKey)}

  Scenario: search personal account not authenticated should return a 401
    * configure headers = null
    Given path '/api/personalaccounts/'+pa1.personalAccount.id+'/key'
    When method GET
    Then status 401



