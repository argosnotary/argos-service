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

Feature: Verification2.0

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
    * def sa2 = defaultTestData.serviceAccounts['default-sa2']
    * def sa3 = defaultTestData.serviceAccounts['default-sa3']
    * def defaultOrganizationId = defaultTestData.defaultOrganization.id;
    * def defaultProjectId = defaultTestData.defaultProject.id;
    * def defaultReleaseArtifacts = {'releaseArtifacts': [[{uri: 'target/argos-test-0.0.1-SNAPSHOT.jar',hash: '49e73a11c5e689db448d866ce08848ac5886cac8aa31156ea4de37427aca6162'}]] }
    * def defaultSteps = [{link:'build-step-link.json', signingAccount:#(sa1)},{link:'test-step-link.json', signingAccount:#(sa2)}]
    * configure headers = call read('classpath:headers.js') { token: #(defaultTestData.ownerToken)}

  Scenario: successfull release should return successfull verify
    * def resp = call read('classpath:feature/release/release-template.feature') { projectId: #(defaultProjectId), releaseArtifacts:#(defaultReleaseArtifacts) ,testDir: 'happy-flow',steps:#(defaultSteps), layoutSigner:#(pa1), account2: #(sa1), account3: #(sa2), releaseAccount: #(sa1) }
    * configure headers = {'Content-Type': 'application/json'}
    Given path '/api/supplychains/verification'
    And param artifactHashes = '49e73a11c5e689db448d866ce08848ac5886cac8aa31156ea4de37427aca6162'
    And param paths = 'org.com'
    When method GET
    Then status 200
    And match response == {"runIsValid":true}

  Scenario: successfull release with incorrect hash should return unsuccessfull verify
    * def resp = call read('classpath:feature/release/release-template.feature') { projectId: #(defaultProjectId), releaseArtifacts:#(defaultReleaseArtifacts) ,testDir: 'happy-flow',steps:#(defaultSteps), layoutSigner:#(pa1), account2: #(sa1), account3: #(sa2), releaseAccount: #(sa1) }
    * configure headers = {'Content-Type': 'application/json'}
    Given path '/api/supplychains/verification'
    And param artifactHashes = '49e73a11c5e689db448d866ce08848ac5886cac8aa31156ea4de37427aca6163'
    And param path = 'default-root-label'
    When method GET
    Then status 200
    And match response == {"runIsValid":false}
    
  Scenario: unknown hash should return unsuccessfull verify
    * def resp = call read('classpath:feature/release/release-template.feature') { projectId: #(defaultProjectId), releaseArtifacts:#(defaultReleaseArtifacts) ,testDir: 'happy-flow',steps:#(defaultSteps), layoutSigner:#(pa1), account2: #(sa1), account3: #(sa2), releaseAccount: #(sa1) }
    * configure headers = {'Content-Type': 'application/json'}
    Given path '/api/supplychains/verification'
    And param artifactHashes = '0123456789012345678901234567890012345678901234567890123456789012'
    And param paths = 'org.com'
    When method GET
    Then status 200
    And match response == {"runIsValid":false}


