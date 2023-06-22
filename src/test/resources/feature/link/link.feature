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
    * configure headers = call read('classpath:headers.js') { token: #(defaultTestData.ownerToken)}
    * def supplyChain = call read('classpath:feature/supplychain/create-supplychain.feature') { supplyChainName: 'name', parentLabelId: #(defaultTestData.defaultRootLabel.id)}
    * def linkPath = '/api/supplychain/'+ supplyChain.response.id + '/link'
    * def validLink = 'classpath:testmessages/link/valid-link.json'
    * def linkToBesigned = read(validLink)

  Scenario: store link with valid specifications should return a 204 and commit to audit log
    * call read('create-link.feature') {supplyChainId:#(supplyChain.response.id), linkFile:#(validLink), signingAccount:#(sa1)}    
    * def auditlog = call getAuditLogs { url: #(mongoUrl)}
    And match auditlog contains 'createLink'
    And match auditlog contains 'signature'
    And match auditlog !contains 'link'

  Scenario: SERVICE_ACCOUNT can store a link with valid specifications and should return a 204
    * def childLabelResult = call read('classpath:feature/label/create-label.feature') {name: child-label, parentLabelId: #(supplyChain.response.parentLabelId)}
    * def otherSupplyChain = call read('classpath:feature/supplychain/create-supplychain.feature') { supplyChainName: 'other', parentLabelId: #(childLabelResult.response.id)}
    * call read('create-link.feature') {supplyChainId:#(otherSupplyChain.response.id), linkFile:#(validLink), signingAccount:#(sa1)}

  Scenario: user with local permission LINK_ADD can store a link
    * def otherSupplyChain = call read('classpath:feature/supplychain/create-supplychain.feature') { supplyChainName: 'other', parentLabelId: #(defaultTestData.defaultRootLabel.id)}
    * def signedLink = call signLink { passphrase: #(sa1.passphrase), keyPair: #(sa1.serviceAccount.activeKeyPair), linkMetaBlock: #(linkToBesigned)}
    * configure headers = call read('classpath:headers.js') { token: #(pa5.token)}
    Given path '/api/supplychain/'+ otherSupplyChain.response.id + '/link'
    And request signedLink
    When method POST
    Then status 204

  Scenario: user without local permission LINK_ADD cannot store a link
    * def label = call read('classpath:feature/label/create-label.feature') { name: 'label0'}
    * def otherSupplyChain = call read('classpath:feature/supplychain/create-supplychain.feature') {supplyChainName: other-supply-chain, parentLabelId: #(label.response.id)}
    * def signedLink = call signLink { passphrase: #(sa1.passphrase), keyPair: #(sa1.serviceAccount.activeKeyPair), linkMetaBlock: #(linkToBesigned)}
    * configure headers = call read('classpath:headers.js') { token: #(pa2.token)}
    Given path '/api/supplychain/'+ otherSupplyChain.response.id + '/link'
    And request signedLink
    When method POST
    Then status 403

  Scenario: SERVICE_ACCOUNT in other root label cannot store a link
    * def otherRootLabel = call read('classpath:feature/label/create-label.feature') { name: 'other-root-label'}
    * def otherSupplyChain = call read('classpath:feature/supplychain/create-supplychain.feature') {supplyChainName: other-supply-chain, parentLabelId: #(otherRootLabel.response.id)}
    * def signedLink = call signLink { passphrase: #(sa1.passphrase), keyPair: #(sa1.serviceAccount.activeKeyPair), linkMetaBlock: #(linkToBesigned)}
    * configure headers = call read('classpath:headers.js') { token: #(sa1.token)}
    Given path '/api/supplychain/'+ otherSupplyChain.response.id + '/link'
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
    * call read('create-link.feature') {supplyChainId:#(supplyChain.response.id), linkFile:#(validLink), signingAccount:#(sa1)}
    * configure headers = call read('classpath:headers.js') { token: #(defaultTestData.ownerToken)}
    Given path linkPath
    When method GET
    Then status 200
    And match response[*] contains read('classpath:testmessages/link/valid-link-response.json')

  Scenario: find link without authorization should return a 401 error
    * call read('create-link.feature') {supplyChainId:#(supplyChain.response.id), linkFile:#(validLink), signingAccount:#(sa1)}
    * configure headers = null
    Given path linkPath
    And header Content-Type = 'application/json'
    When method GET
    Then status 401

  Scenario: find link with valid supplychainid and optionalHash should return a 200
    * call read('create-link.feature') {supplyChainId:#(supplyChain.response.id), linkFile:#(validLink), signingAccount:#(sa1)}
    * configure headers = call read('classpath:headers.js') { token: #(defaultTestData.ownerToken)}
    Given path linkPath
    And param optionalHash = '74a88c1cb96211a8f648af3509a1207b2d4a15c0202cfaa10abad8cc26300c63'
    When method GET
    Then status 200
    And match response[*] contains read('classpath:testmessages/link/valid-link-response.json')

  Scenario: user with READ local permission can find link with valid supplychainid and optionalHash should return a 200
    * call read('create-link.feature') {supplyChainId:#(supplyChain.response.id), linkFile:#(validLink), signingAccount:#(sa1)}
    * configure headers = call read('classpath:headers.js') { token: #(pa2.token)}
    Given path linkPath
    And param optionalHash = '74a88c1cb96211a8f648af3509a1207b2d4a15c0202cfaa10abad8cc26300c63'
    When method GET
    Then status 200
    And match response[*] contains read('classpath:testmessages/link/valid-link-response.json')

  Scenario: user without READ local permission cannot find link with valid supplychainid and optionalHash should return a 403
    * call read('create-link.feature') {supplyChainId:#(supplyChain.response.id), linkFile:#(validLink), signingAccount:#(sa1)}
    * configure headers = call read('classpath:headers.js') { token: #(pa4.token)}
    Given path linkPath
    And param optionalHash = '74a88c1cb96211a8f648af3509a1207b2d4a15c0202cfaa10abad8cc26300c63'
    When method GET
    Then status 403
