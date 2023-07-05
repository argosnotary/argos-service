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

Feature: Hierarchy

  Background:
    * url karate.properties['server.baseurl']
    * def defaultTestData = call read('classpath:default-test-data.js')
    * url karate.properties['server.baseurl']  
    * call read('classpath:common.feature')
    * reset()
    * def defaultTestData = call read('classpath:default-test-data.js')
    * configure headers = call read('classpath:headers.js') { token: #(defaultTestData.ownerToken)}
    * def pa4 = defaultTestData.personalAccounts['default-pa4']
    * def root1 = call read('classpath:feature/label/create-label.feature') { name: 'root1'}
    * def root2 = call read('classpath:feature/label/create-label.feature') { name: 'root2'}
    * def root3 = call read('classpath:feature/label/create-label.feature') { name: 'root3'}
    * def personalAccount1 = paLogin('user6')
    * def paPermissions = [READ, TREE_EDIT, LOCAL_PERMISSION_EDIT]
    * call read('classpath:feature/account/set-local-permissions.feature') {accountId: #(personalAccount1.personalAccount.id), labelId: #(root1.response.id), permissions: #(paPermissions)}
    * call read('classpath:feature/account/set-local-permissions.feature') {accountId: #(personalAccount1.personalAccount.id), labelId: #(root2.response.id), permissions: #(paPermissions)}
    * call read('classpath:feature/account/set-local-permissions.feature') {accountId: #(personalAccount1.personalAccount.id), labelId: #(root3.response.id), permissions: #(paPermissions)}
    
    * configure headers = call read('classpath:headers.js') { token: #(personalAccount1.token)}
    * def childARoot1Response = call read('classpath:feature/label/create-label.feature') { name: 'childaroot1',parentLabelId:#(root1.response.id)}
    * def childARoot2Response = call read('classpath:feature/label/create-label.feature') { name: 'childaroot2',parentLabelId:#(root2.response.id)}
    * def childARoot3Response = call read('classpath:feature/label/create-label.feature') { name: 'childaroot3',parentLabelId:#(root3.response.id)}
    * def supplyChain1Response = call read('classpath:feature/supplychain/create-supplychain.feature') {supplyChainName: supply-chain-1, parentLabelId: #(childARoot1Response.response.id)}
    * def supplyChain2Response = call read('classpath:feature/supplychain/create-supplychain.feature') {supplyChainName: supply-chain-2, parentLabelId: #(childARoot2Response.response.id)}
    * def supplyChain3Response = call read('classpath:feature/supplychain/create-supplychain.feature') {supplyChainName: supply-chain-3, parentLabelId: #(childARoot3Response.response.id)}
    * def serviceAccount1Response = call read('classpath:feature/account/create-service-account.feature') {userName: sa-1, parentLabelId: #(childARoot1Response.response.id)}
    * def serviceAccount2Response = call read('classpath:feature/account/create-service-account.feature') {userName: sa-2, parentLabelId: #(childARoot2Response.response.id)}
    * def serviceAccount3Response = call read('classpath:feature/account/create-service-account.feature') {userName: sa-3, parentLabelId: #(childARoot3Response.response.id)}
    * def childBRoot1Response = call read('classpath:feature/label/create-label.feature') { name: 'childbroot1',parentLabelId:#(root1.response.id)}
    * def childBRoot2Response = call read('classpath:feature/label/create-label.feature') { name: 'childbroot2',parentLabelId:#(root2.response.id)}
    * def childBRoot3Response = call read('classpath:feature/label/create-label.feature') { name: 'childbroot3',parentLabelId:#(root3.response.id)}
    

  Scenario: get root nodes with HierarchyMode all should return full trees
    * def expectedResponse =  read('classpath:testmessages/hierarchy/expected-hierarchy-rootnodes-all.json')
    * configure headers = call read('classpath:headers.js') { token: #(personalAccount1.token)}
    Given path '/api/hierarchy'
    And param HierarchyMode = 'ALL'
    When method GET
    Then status 200
    And match response == expectedResponse

  Scenario: get root nodes with default user and no hierarchy permissions should return empty array
    * configure headers = call read('classpath:headers.js') { token: #(pa4.token)}
    Given path '/api/hierarchy'
    And param HierarchyMode = 'ALL'
    When method GET
    Then status 200
    And match response == []

  Scenario: get root nodes without authorization should return a 401 error
    * configure headers = null
    Given path '/api/hierarchy'
    And param HierarchyMode = 'ALL'
    When method GET
    Then status 401

  Scenario: get root nodes with HierarchyMode none should return root entries only
    * configure headers = call read('classpath:headers.js') { token: #(personalAccount1.token)}
    Given path '/api/hierarchy'
    And param HierarchyMode = 'NONE'
    When method GET
    Then status 200
    * def expectedResponse =  read('classpath:testmessages/hierarchy/expected-hierarchy-rootnodes-none.json')
    And match response == expectedResponse

  Scenario: get root nodes with HierarchyMode maxdepth should return maxdepth descendant entries only
    * configure headers = call read('classpath:headers.js') { token: #(personalAccount1.token)}
    * def subchild1root1Response = call read('classpath:feature/label/create-label.feature') { name: 'subchild1root1',parentLabelId:#(childARoot1Response.response.id)}
    Given path '/api/hierarchy'
    And param HierarchyMode = 'MAX_DEPTH'
    And param maxDepth = 1
    When method GET
    Then status 200
    * def expectedResponse =  read('classpath:testmessages/hierarchy/expected-hierarchy-rootnodes-maxdepth.json')
    And match response == expectedResponse

  Scenario: get root nodes with HierarchyMode maxdepth and non positive maxdepth should return validation error
    * configure headers = call read('classpath:headers.js') { token: #(personalAccount1.token)}
    Given path '/api/hierarchy'
    And param HierarchyMode = 'MAX_DEPTH'
    And param maxDepth = -1
    When method GET
    Then status 400
    And match response == {"messages": [{"field": "getRootNodes.maxDepth","type": "DATA_INPUT","message": "must be greater than or equal to 1"}]}

  Scenario: get root nodes with HierarchyMode maxdepth and no maxdepth should return maxdepth 1 descendant entries only
    * configure headers = call read('classpath:headers.js') { token: #(personalAccount1.token)}
    * def subchild1child3root1Response = call read('classpath:feature/label/create-label.feature') { name: 'subchild1child3root1',parentLabelId:#(childARoot1Response.response.id)}
    Given path '/api/hierarchy'
    And param HierarchyMode = 'MAX_DEPTH'
    When method GET
    Then status 200
    * def expectedResponse =  read('classpath:testmessages/hierarchy/expected-hierarchy-rootnodes-maxdepth.json')
    And match response == expectedResponse

  Scenario: get root nodes with no permissions should return only root nodes with permissions
    * def personalAccount2 = paLogin('user7')
    * configure headers = call read('classpath:headers.js') { token: #(personalAccount1.token)}
    * def localPermissionsForRoot = call read('classpath:feature/account/set-local-permissions.feature') { accountId: #(personalAccount2.personalAccount.id),labelId: #(root1.response.id), permissions: ["READ"]}
    * configure headers = call read('classpath:headers.js') { token: #(personalAccount2.token)}
    Given path '/api/hierarchy'
    And param HierarchyMode = 'NONE'
    When method GET
    Then status 200
    * def expectedResponse =  read('classpath:testmessages/hierarchy/expected-hierarchy-rootnodes-partial-permissions.json')
    And match response == expectedResponse

  Scenario: get subtree with HierarchyMode all should return full tree
    * configure headers = call read('classpath:headers.js') { token: #(personalAccount1.token)}
    Given path '/api/hierarchy/' + root1.response.id
    And param HierarchyMode = 'ALL'
    When method GET
    Then status 200
    * def expectedResponse =  read('classpath:testmessages/hierarchy/expected-hierarchy-subtree-all.json')
    And match response == expectedResponse

  Scenario: get subtree with added permissions downtree should return correct permissions
    * def personalAccount2 = paLogin('user7')
    * configure headers = call read('classpath:headers.js') { token: #(personalAccount1.token)}
    * def localPermissionsForRoot = call read('classpath:feature/account/set-local-permissions.feature') { accountId: #(personalAccount2.personalAccount.id),labelId: #(root1.response.id), permissions: ["READ"]}
    * def root1ChildPermissions = call read('classpath:feature/account/set-local-permissions.feature') { accountId: #(personalAccount2.personalAccount.id),labelId: #(childARoot1Response.response.id), permissions: ["LOCAL_PERMISSION_EDIT"]}
    * configure headers = call read('classpath:headers.js') { token: #(personalAccount2.token)}
    Given path '/api/hierarchy/' + root1.response.id
    And param HierarchyMode = 'ALL'
    When method GET
    Then status 200
    * def expectedResponse =  read('classpath:testmessages/hierarchy/expected-hierarchy-subtree-added-local-permissions.json')
    And match response == expectedResponse

  Scenario: get subtree with permissions uptree should return correct partial hierarchy
    * def personalAccount2 = paLogin('user7')
    * configure headers = call read('classpath:headers.js') { token: #(personalAccount1.token)}
    * def root1ChildPermissions = call read('classpath:feature/account/set-local-permissions.feature') { accountId: #(personalAccount2.personalAccount.id),labelId: #(childARoot1Response.response.id), permissions: ["LOCAL_PERMISSION_EDIT"]}
    * configure headers = call read('classpath:headers.js') { token: #(personalAccount2.token)}
    Given path '/api/hierarchy/' + root1.response.id
    And param HierarchyMode = 'ALL'
    When method GET
    Then status 200
    * def expectedResponse =  read('classpath:testmessages/hierarchy/expected-hierarchy-subtree-partial-hierarchy.json')
    And match response == expectedResponse

  Scenario: get subtree with HierarchyMode none should return only root
    * configure headers = call read('classpath:headers.js') { token: #(personalAccount1.token)}
    Given path '/api/hierarchy/' + root1.response.id
    And param HierarchyMode = 'NONE'
    When method GET
    Then status 200
    * def expectedResponse =  read('classpath:testmessages/hierarchy/expected-hierarchy-subtree-none.json')
    And match response == expectedResponse

  Scenario: get subtree with HierarchyMode max depth 1 should return only direct descendants
    * configure headers = call read('classpath:headers.js') { token: #(personalAccount1.token)}
    * def subchild1child3root1 = call read('classpath:feature/label/create-label.feature') { name: 'subchild1child3root1',parentLabelId:#(childARoot1Response.response.id)}
    Given path '/api/hierarchy/' + root1.response.id
    And param HierarchyMode = 'MAX_DEPTH'
    And param maxDepth = 1
    When method GET
    Then status 200
    * def expectedResponse =  read('classpath:testmessages/hierarchy/expected-hierarchy-subtree-maxdepth.json')
    And match response == expectedResponse

  Scenario: get subtree with HierarchyMode max depth -1 should return a validation error
    * configure headers = call read('classpath:headers.js') { token: #(personalAccount1.token)}
    Given path '/api/hierarchy/' + root1.response.id
    And param HierarchyMode = 'MAX_DEPTH'
    And param maxDepth = -1
    When method GET
    Then status 400
    And match response ==  {"messages": [{"field": "getSubTree.maxDepth","type": "DATA_INPUT","message": "must be greater than or equal to 1"}]}

  Scenario: get subtree with HierarchyMode maxdepth and no maxdepth should return maxdepth 1 descendant entries only
    * configure headers = call read('classpath:headers.js') { token: #(personalAccount1.token)}
    * call read('classpath:feature/label/create-label.feature') { name: 'subchild1child3root1',parentLabelId:#(childARoot1Response.response.id)}
    Given path '/api/hierarchy/' + root1.response.id
    And param HierarchyMode = 'MAX_DEPTH'
    When method GET
    Then status 200
    * def expectedResponse =  read('classpath:testmessages/hierarchy/expected-hierarchy-subtree-maxdepth.json')
    And match response == expectedResponse
 