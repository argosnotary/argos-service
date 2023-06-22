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

Feature: Label

  Background:
    * url karate.properties['server.baseurl']   
    * call read('classpath:common.feature')
    * reset()  
    * def defaultTestData = call read('classpath:default-test-data.js')
    * configure headers = call read('classpath:headers.js') { token: #(defaultTestData.ownerToken)}
    * def mongoUrl = call read('classpath:mongo-url.js')

  Scenario: store a root label with valid name should return a 201 and commit to auditlog
    * def result = call read('create-label.feature') { name: 'label0'}
    * match result.response == { name: 'label0', id: '#uuid' }
    * def auditlog = call getAuditLogs { url: #(mongoUrl)}
    And match auditlog contains 'createLabel'
    And match auditlog contains 'label'

  Scenario: store a root label without TREE_EDIT permission name should return a 403
    * def userWithoutPermissions = defaultTestData.personalAccounts['default-pa1']
    * configure headers = call read('classpath:headers.js') { token: #(userWithoutPermissions.token)}
    Given path '/api/label'
    And request { name: 'label1'}
    When method POST
    Then status 403

  Scenario: store a root label with invalid name should return a 400
    Given path '/api/label'
    And request { name: '1label'}
    When method POST
    Then status 400
    And match response.messages[0].message == 'must match "^([a-z]|[a-z][a-z0-9-]*[a-z0-9])?$"'

  Scenario: store a label without authorization should return a 401 error
    * configure headers = null
    Given path '/api/label'
    And request { name: 'label1'}
    When method POST
    Then status 401

  Scenario: store two root labels with the same name should return a 400
    * def result = call read('create-label.feature') { name: 'label1'}
    * match result.response == { name: 'label1', id: '#uuid' }
    Given path '/api/label'
    And request { name: 'label1'}
    When method POST
    Then status 400
    And match response.messages[0].message == 'label with name: label1 and parentLabelId: null already exists'


  Scenario: retrieve root label should return a 200
    * def result = call read('create-label.feature') { name: 'label2'}
    * def restPath = '/api/label/'+result.response.id
    Given path restPath
    When method GET
    Then status 200
    And match response == { name: 'label2', id: '#(result.response.id)' }

  Scenario: retrieve root label should without READ permission should return a 403
    * def result = call read('create-label.feature') { name: 'label3'}
    * def restPath = '/api/label/'+result.response.id
    * def userWithoutPermissions = defaultTestData.personalAccounts['default-pa1']
    * configure headers = call read('classpath:headers.js') { token: #(userWithoutPermissions.token)}
    Given path restPath
    When method GET
    Then status 403

  Scenario: retrieve a label without authentication should return a 401 error
    * def result = call read('create-label.feature') { name: 'label4'}
    * def restPath = '/api/label/'+result.response.id
    * configure headers = null
    Given path restPath
    When method GET
    Then status 401

  Scenario: update a root label should return a 200 and commit to audit log
    * def createResult = call read('create-label.feature') { name: 'label5'}
    * def labelId = createResult.response.id
    * def restPath = '/api/label/'+labelId
    Given path restPath
    And request { name: 'label5'}
    When method PUT
    Then status 200
    And match response == { name: 'label5', id: '#(labelId)' }
    * def auditlog = call getAuditLogs { url: #(mongoUrl)}
    And match auditlog contains 'updateLabelById'
    And match auditlog contains 'label'
    And match auditlog contains 'labelId'


  Scenario: update a label without authorization should return a 401 error
    * def restPath = '/api/label/iets'
    * configure headers = null
    Given path restPath
    And request { name: 'label4'}
    When method PUT
    Then status 401

  Scenario: store a child label with valid name should return a 201
    * def rootLabelResponse = call read('create-label.feature') { name: 'parent1'}
    * def rootId = rootLabelResponse.response.id
    * def childLabelResponse = call read('create-label.feature') { name: 'child', parentLabelId: '#(rootId)'}
    * match childLabelResponse.response == { name: 'child', id: '#uuid', parentLabelId: '#(rootId)'}

  Scenario: delete a child label should return a 204
    * def rootLabelResponse = call read('create-label.feature') { name: 'parent2'}
    * def rootId = rootLabelResponse.response.id
    * def childLabelResponse = call read('create-label.feature') { name: 'child', parentLabelId: '#(rootId)'}
    * def childId = childLabelResponse.response.id
    * def restPath = '/api/label/'+childId
    Given path restPath
    When method DELETE
    Then status 204

  Scenario: delete a root label should return a 204
    * def rootLabelResponse = call read('create-label.feature') { name: 'parent3'}
    * def rootId = rootLabelResponse.response.id
    * def childLabelResponse = call read('create-label.feature') { name: 'child', parentLabelId: '#(rootId)'}
    * def rootRestPath = '/api/label/'+rootId
    Given path rootRestPath
    When method DELETE
    Then status 204
    * def childRestPath = '/api/label/'+childLabelResponse.response.id
    Given path childRestPath
    When method DELETE
    Then status 404
    * def auditlog = call getAuditLogs { url: #(mongoUrl)}
    And match auditlog contains 'deleteLabelById'
    And match auditlog contains 'label'

  Scenario: delete a child label without TREE_EDIT local permission should return a 403
    * def rootLabelResponse = call read('create-label.feature') { name: 'parent4'}
    * def rootId = rootLabelResponse.response.id
    * def childLabelResponse = call read('create-label.feature') { name: 'child', parentLabelId: '#(rootId)'}
    * def childId = childLabelResponse.response.id
    * def restPath = '/api/label/'+childId
    * def userWithoutPermissions = defaultTestData.personalAccounts['default-pa1']
    * configure headers = call read('classpath:headers.js') { token: #(userWithoutPermissions.token)}
    Given path restPath
    When method DELETE
    Then status 403

  Scenario: retrieve child label should return a 200
    * def rootLabelResponse = call read('create-label.feature') { name: 'parent5'}
    * def rootId = rootLabelResponse.response.id
    * def childLabelResponse = call read('create-label.feature') { name: 'child', parentLabelId: '#(rootId)'}
    * def childId = childLabelResponse.response.id
    * def restPath = '/api/label/'+childId
    Given path restPath
    When method GET
    Then status 200
    And match response == { name: 'child', id: '#(childId)', parentLabelId: '#(rootId)' }

  Scenario: update a child label should return a 200
    * def rootLabelResponse = call read('create-label.feature') { name: 'parent6'}
    * def rootId = rootLabelResponse.response.id
    * def childLabelResponse = call read('create-label.feature') { name: 'child', parentLabelId: '#(rootId)'}
    * def childId = childLabelResponse.response.id
    * def restPath = '/api/label/'+childId
    Given path restPath
    And request { name: 'label4', parentLabelId: '#(rootId)'}
    When method PUT
    Then status 200
    And match response == { name: 'label4', id: '#(childId)', parentLabelId: '#(rootId)' }

  Scenario: update a child label without any TREE_EDIT local permission should return a 403
    * def rootLabelResponse = call read('create-label.feature') { name: 'parent'}
    * def rootId = rootLabelResponse.response.id
    * def childLabelResponse = call read('create-label.feature') { name: 'child', parentLabelId: '#(rootId)'}
    * def childId = childLabelResponse.response.id
    * def restPath = '/api/label/'+childId
    * def userWithoutPermissions = defaultTestData.personalAccounts['default-pa1']
    * configure headers = call read('classpath:headers.js') { token: #(userWithoutPermissions.token)}
    Given path restPath
    And request { name: 'label4', parentLabelId: '#(rootId)'}
    When method PUT
    Then status 403


