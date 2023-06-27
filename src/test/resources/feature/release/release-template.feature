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

@Ignore
Feature: Verification template

  Background:
    * url karate.properties['server.baseurl']
    
    Given path '/api/nodes/'+projectId+'/supplychains'
    And request { name: 'name', parentId: #(projectId)}
    When method POST
    Then status 201
    * def supplyChainId = response.id
    * def supplyChainPath = '/api/supplychains/'+ supplyChainId
    # variables for substitution
    * def layoutKey = layoutSigner.personalAccount.activeKeyPair
    * def key2 = account2.serviceAccount.activeKeyPair
    * def key3 = account3.serviceAccount.activeKeyPair
    
    * def layoutPath = '/api/supplychains/'+ supplyChainId + '/layout'
    * def linkPath = '/api/supplychains/'+ supplyChainId + '/link'

  Scenario: run template
    Given print 'testDir : ', testDir
    * def layout = read('classpath:testmessages/verification/'+testDir+'/layout.json')
    * def layoutCreated = call read('classpath:feature/layout/create-layout.feature') {supplyChainId:#(supplyChainId), layoutToBeSigned:#(layout), layoutSigner:#(layoutSigner)}
    # this creates an array of stepLinksJson messages
    * def stepLinksJsonMapper = function(jsonlink, i){ return  {supplyChainId:supplyChainId, linkFile:'classpath:testmessages/verification/'+testDir+'/'+jsonlink.link, signingAccount:jsonlink.signingAccount}}
    * def stepLinksJson = karate.map(steps, stepLinksJsonMapper)
    # when a call to a feature presented with an array of messages it will cal the feature template iteratively
    * call read('classpath:feature/link/create-link.feature') stepLinksJson
    * configure headers = call read('classpath:headers.js') { token: #(releaseAccount.token)}
    Given path supplyChainPath + '/release'
    And request  releaseArtifacts
    When method POST
    Then status 200
    