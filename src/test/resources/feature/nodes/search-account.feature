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

Feature: Search Account

  Background:
    * url karate.properties['server.baseurl']    
    * call read('classpath:common.feature')
    * reset()  
    * def defaultTestData = call read('classpath:default-test-data.js')
    * def defaultTestData = call read('classpath:default-test-data.js')
    * def pa1 = defaultTestData.personalAccounts['default-pa1']
    * def pa2 = defaultTestData.personalAccounts['default-pa2']
    * def pa3 = defaultTestData.personalAccounts['default-pa3']
    * def pa4 = defaultTestData.personalAccounts['default-pa4']
    * def pa5 = defaultTestData.personalAccounts['default-pa5']
    * def sa1 = defaultTestData.serviceAccounts['default-sa1']
    * def keyId = defaultTestData.serviceAccounts['default-sa2'].serviceAccount.activeKeyPair.keyId
    * configure headers = call read('classpath:headers.js') { token: #(defaultTestData.ownerToken)}
    * def supplyChain = call read('classpath:feature/supplychain/create-supplychain.feature') { supplyChainName: 'name', parentLabelId: #(defaultTestData.defaultRootLabel.id)}

  Scenario: search account by key id should return a 200
    Given path '/api/supplychain/'+supplyChain.response.id+'/account/key'
    And param keyIds = keyId
    When method GET
    Then status 200
    * def expectedResponse = read('classpath:testmessages/account/search-account-keyinfo-response.json')
    And match response contains expectedResponse

  Scenario: search account by key id without READ should return a 403
    * configure headers = call read('classpath:headers.js') { token: #(pa4.token)}
    Given path '/api/supplychain/'+supplyChain.response.id+'/account/key'
    And param keyIds = keyId
    When method GET
    Then status 403

  Scenario: search account by name should return a 200
    Given path '/api/supplychain/'+supplyChain.response.id+'/account'
    And param name = "default-sa1"
    When method GET
    Then status 200
    * def expectedResponse = read('classpath:testmessages/account/search-account-info-response.json')
    And match response contains expectedResponse

  Scenario: search account without READ should return a 403
    * configure headers = call read('classpath:headers.js') { token: #(pa4.token)}
    Given path '/api/supplychain/'+supplyChain.response.id+'/account'
    And param name = "default-sa1"
    When method GET
    Then status 403

  Scenario: search account by name not in path should return a 200 with empty array
    * configure headers = call read('classpath:headers.js') { token: #(defaultTestData.ownerToken)}
    Given path '/api/supplychain/'+supplyChain.response.id+'/account'
    And param name = 'not-in-path'
    When method GET
    Then status 200
    And match response == []
