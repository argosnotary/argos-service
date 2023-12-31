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

Feature: Verification

  Background:
    * call read('classpath:common.feature')
    * reset()
    * def defaultTestData = call read('classpath:default-test-data.js')
    * def pa1 = defaultTestData.personalAccounts['default-pa1']
    * def pa4 = defaultTestData.personalAccounts['default-pa4']
    * def sa1 = defaultTestData.serviceAccounts['default-sa1']
    * def sa2 = defaultTestData.serviceAccounts['default-sa2']
    * def sa3 = defaultTestData.serviceAccounts['default-sa3']
    * def defaultOrganizationId = defaultTestData.defaultOrganization.id;
    * def defaultProjectId = defaultTestData.defaultProject.id;
    * def defaultExpectedProducts = [{uri: 'target/argos-test-0.0.1-SNAPSHOT.jar',hash: '49e73a11c5e689db448d866ce08848ac5886cac8aa31156ea4de37427aca6162'}]
    * def defaultSteps = [{link:'build-step-link.json', signingAccount:#(sa1)},{link:'test-step-link.json', signingAccount:#(sa2)}]
    * def defaultTestData = call read('classpath:default-test-data.js')
    * configure headers = call read('classpath:headers.js') { token: #(defaultTestData.ownerToken)}
  
  Scenario: happy flow all rules and commit to audit log
    * def resp = call read('classpath:feature/verification/verification-template.feature') { projectId: #(defaultProjectId), expectedProducts:#(defaultExpectedProducts), testDir: 'happy-flow', steps:#(defaultSteps), layoutSigner:#(pa1), account2: #(sa1), account3: #(sa2) }
    And match resp.response == {"runIsValid":true}
    
  Scenario: products to verify wrong hash
    * def expectedProducts = [{uri: 'target/argos-test-0.0.1-SNAPSHOT.jar',hash: '0123456789012345678901234567890012345678901234567890123456789012'}] 
    * def resp = call read('classpath:feature/verification/verification-template.feature') { projectId: #(defaultProjectId), expectedProducts:#(expectedProducts) ,testDir: 'happy-flow',steps:#(defaultSteps), layoutSigner:#(pa1), account2: #(sa1), account3: #(sa2) }
    And match resp.response == {"runIsValid":false}

  Scenario: expected expected end products not matches
    * def expectedProducts = [{uri: 'argos-test-0.0.1-SNAPSHOT.jar',hash: '49e73a11c5e689db448d866ce08848ac5886cac8aa31156ea4de37427aca6162'}]
    * def resp = call read('classpath:feature/verification/verification-template.feature') { projectId: #(defaultProjectId), expectedProducts:#(expectedProducts) ,testDir: 'happy-flow',steps:#(defaultSteps), layoutSigner:#(pa1), account2: #(sa1), account3: #(sa2) }
    And match resp.response == {"runIsValid":false}

  Scenario: multi step happy flow all rules
    * def steps = [{link:'build-step1-link.json', signingAccount:#(sa2)},{link:'test-step1-link.json', signingAccount:#(sa2)},{link:'build-step2-link.json', signingAccount:#(sa3)},{link:'test-step2-link.json',signingAccount:#(sa3)}]
    * def verificationRequest = {expectedProducts: [{uri: 'target/argos-test-0.0.1-SNAPSHOT.jar',hash: '49e73a11c5e689db448d866ce08848ac5886cac8aa31156ea4de37427aca6162'}] }
    * def resp = call read('classpath:feature/verification/verification-template.feature') { projectId: #(defaultProjectId), expectedProducts:#(defaultExpectedProducts) ,testDir: 'multi-step-happy-flow',steps:#(steps), layoutSigner:#(pa1), account2: #(sa2), account3: #(sa3) }
    And match resp.response == {"runIsValid":true}

  Scenario: multi step happy flow with three step hop
    * def steps = [{link:'build-step1-link.json', signingAccount:#(sa2)},{link:'test-step1-link.json', signingAccount:#(sa3)},{link:'build-step2-link.json', signingAccount:#(sa2)},{link:'test-step2-link.json',signingAccount:#(sa3)},{link:'build-step3-link.json', signingAccount:#(sa2)},{link:'test-step3-link.json', signingAccount:#(sa3)}]
    * def verificationRequest = {expectedProducts: [ {uri: 'target/argos-test-0.0.1-SNAPSHOT.jar',hash: '49e73a11c5e689db448d866ce08848ac5886cac8aa31156ea4de37427aca6162'}] }
    * def resp = call read('classpath:feature/verification/verification-template.feature') { projectId: #(defaultProjectId), expectedProducts:#(defaultExpectedProducts) ,testDir: 'multi-step-happy-flow-with-three-step-hop',steps:#(steps), layoutSigner:#(pa1), account2: #(sa2), account3: #(sa3) }
    And match resp.response == {"runIsValid":true}

  Scenario: multi step with multiple verification context
    * def steps = [{link:'build-step1-link.json', signingAccount:#(sa2)},{link:'test-step1-link.json', signingAccount:#(sa2)},{link:'build-step2-link.json', signingAccount:#(sa3)},{link:'build-step2-link-invalid.json', signingAccount:#(sa3)},{link:'test-step2-link.json',signingAccount:#(sa3)},{link:'test-step2-link-invalid.json',signingAccount:#(sa3)},{link:'build-step3-link.json', signingAccount:#(sa2)},{link:'test-step3-link.json', signingAccount:#(sa2)}]
    * def verificationRequest = {expectedProducts: [ {uri: 'target/argos-test-0.0.1-SNAPSHOT.jar',hash: '49e73a11c5e689db448d866ce08848ac5886cac8aa31156ea4de37427aca6162'}] }
    * def resp = call read('classpath:feature/verification/verification-template.feature') { projectId: #(defaultProjectId), expectedProducts:#(defaultExpectedProducts) ,testDir: 'multi-step-with-multiple-verification-context',steps:#(steps), layoutSigner:#(pa1), account2: #(sa2), account3: #(sa3) }
    And match resp.response == {"runIsValid":true}

  Scenario: happy flow match-rule-happy-flow
    * def resp = call read('classpath:feature/verification/verification-template.feature') { projectId: #(defaultProjectId), expectedProducts:#(defaultExpectedProducts) ,testDir: 'match-rule-happy-flow',steps:#(defaultSteps), layoutSigner:#(pa1), account2: #(sa1), account3: #(sa2) }
    And match resp.response == {"runIsValid":true}

  Scenario: happy flow match-rule-happy-flow-with-prefix
    * def resp = call read('classpath:feature/verification/verification-template.feature') { projectId: #(defaultProjectId), expectedProducts:#(defaultExpectedProducts) ,testDir: 'match-rule-happy-flow-with-prefix',steps:#(defaultSteps), layoutSigner:#(pa1), account2: #(sa1), account3: #(sa2) }
    And match resp.response == {"runIsValid":true}

  Scenario: happy flow match-rule-no-destination-artifact
    * def resp = call read('classpath:feature/verification/verification-template.feature') { projectId: #(defaultProjectId), expectedProducts:#(defaultExpectedProducts),testDir: 'match-rule-no-destination-artifact',steps:#(defaultSteps), layoutSigner:#(pa1), account2: #(sa1), account3: #(sa2) }
    And match resp.response == {"runIsValid":false}

  Scenario: happy flow match-rule-no-source-artifact
    * def resp = call read('classpath:feature/verification/verification-template.feature') { projectId: #(defaultProjectId), expectedProducts:#(defaultExpectedProducts),testDir: 'match-rule-no-source-artifact',steps:#(defaultSteps), layoutSigner:#(pa1), account2: #(sa1), account3: #(sa2) }
    And match resp.response == {"runIsValid":false}

  Scenario: build-steps-incomplete-run
    * def resp = call read('classpath:feature/verification/verification-template.feature') { projectId: #(defaultProjectId), expectedProducts:#(defaultExpectedProducts),testDir: 'build-steps-incomplete-run',steps:#(defaultSteps), layoutSigner:#(pa1), account2: #(sa1), account3: #(sa2) }
    And match resp.response == {"runIsValid":false}

  Scenario: delete-rule-no-deletion
    * def resp = call read('classpath:feature/verification/verification-template.feature')  { projectId: #(defaultProjectId), expectedProducts:#(defaultExpectedProducts),testDir: 'delete-rule-no-deletion',steps:#(defaultSteps), layoutSigner:#(pa1), account2: #(sa1), account3: #(sa2) }
    And match resp.response == {"runIsValid":false}

  Scenario: create-rule-no-creation
    * def resp = call read('classpath:feature/verification/verification-template.feature')  { projectId: #(defaultProjectId), expectedProducts:#(defaultExpectedProducts),testDir: 'create-rule-no-creation',steps:#(defaultSteps), layoutSigner:#(pa1), account2: #(sa1), account3: #(sa2) }
    And match resp.response == {"runIsValid":false}

  Scenario: modify-rule-not-modified
    * def resp = call read('classpath:feature/verification/verification-template.feature')  { projectId: #(defaultProjectId), expectedProducts:#(defaultExpectedProducts),testDir: 'modify-rule-not-modified',steps:#(defaultSteps), layoutSigner:#(pa1), account2: #(sa1), account3: #(sa2) }
    And match resp.response == {"runIsValid":false}

  Scenario: require-rule-no-required-product-material
    * def resp = call read('classpath:feature/verification/verification-template.feature') { projectId: #(defaultProjectId), expectedProducts:#(defaultExpectedProducts),testDir: 'require-rule-no-required-product-material',steps:#(defaultSteps), layoutSigner:#(pa1), account2: #(sa1), account3: #(sa2) }
    And match resp.response == {"runIsValid":false}

  Scenario: disallow-rule-non-empty
    * def resp = call read('classpath:feature/verification/verification-template.feature') { projectId: #(defaultProjectId), expectedProducts:#(defaultExpectedProducts),testDir: 'disallow-rule-non-empty',steps:#(defaultSteps), layoutSigner:#(pa1), account2: #(sa1), account3: #(sa2) }
    And match resp.response == {"runIsValid":false}

  Scenario: allow-rule-no-match
    * def resp = call read('classpath:feature/verification/verification-template.feature') { projectId: #(defaultProjectId), expectedProducts:#(defaultExpectedProducts),testDir: 'allow-rule-no-match',steps:#(defaultSteps), layoutSigner:#(pa1), account2: #(sa1), account3: #(sa2) }
    And match resp.response == {"runIsValid":false}

  Scenario: multiple-run-id-happy-flow
    * def steps = [{link:'runid1-build-step-link.json', signingAccount:#(sa2)},{link:'runid1-test-step-link.json', signingAccount:#(sa3)},{link:'runid2-build-step-link.json', signingAccount:#(sa2)},{link:'runid2-test-step-link.json',signingAccount:#(sa3)}]
    * def resp = call read('classpath:feature/verification/verification-template.feature') { projectId: #(defaultProjectId), expectedProducts:#(defaultExpectedProducts),testDir: 'multiple-run-id-happy-flow',steps:#(steps), layoutSigner:#(pa1), account2: #(sa2), account3: #(sa3) }
    And match resp.response == {"runIsValid":true}

  Scenario: multiple-link-files-per-step-one-invalid
    * def steps = [{link:'build-step-link1.json', signingAccount:#(sa2)},{link:'build-step-link2.json', signingAccount:#(sa2)},{link:'test-step-link1.json', signingAccount:#(sa3)},{link:'test-step-link2.json',signingAccount:#(sa3)}]
    * def resp = call read('classpath:feature/verification/verification-template.feature') { projectId: #(defaultProjectId), expectedProducts:#(defaultExpectedProducts),testDir: 'multiple-link-files-per-step-one-invalid',steps:#(steps), layoutSigner:#(pa1), account2: #(sa2), account3: #(sa3) }
    And match resp.response == {"runIsValid":true}

  Scenario: multiple-verification-contexts-happy-flow
    * def steps = [{link:'build-step-link-valid.json', signingAccount:#(sa2)},{link:'build-step-link-invalid.json', signingAccount:#(sa3)},{link:'test-step-link-invalid.json', signingAccount:#(sa2)},{link:'test-step-link-valid.json',signingAccount:#(sa3)}]
    * def resp = call read('classpath:feature/verification/verification-template.feature') { projectId: #(defaultProjectId), expectedProducts:#(defaultExpectedProducts),testDir: 'multiple-verification-contexts',steps:#(steps), layoutSigner:#(pa1), account2: #(sa2), account3: #(sa3) }
    And match resp.response == {"runIsValid":true}
   
  Scenario: verification without authorization should return a 403 error
    * url karate.properties['server.baseurl']
    Given path '/api/nodes/'+defaultProjectId+'/supplychains'
    And request { name: 'name', parentId: #(defaultProjectId)}
    When method POST
    Then status 201
    * def supplyChainId = response.id
    * def supplyChainPath = '/api/supplychains/'+ supplyChainId
    * configure headers = null
    Given path supplyChainPath + '/verification'
    And request defaultExpectedProducts
    When method POST
    Then status 403

  Scenario: verification without permission READ should return a 403 error
    * url karate.properties['server.baseurl']
    Given path '/api/nodes/'+defaultProjectId+'/supplychains'
    And request { name: 'name', parentId: #(defaultProjectId)}
    When method POST
    Then status 201
    * def supplyChainId = response.id
    * def supplyChainPath = '/api/supplychains/'+ supplyChainId
    * configure headers = call read('classpath:headers.js') { token: #(pa4.token)}
    Given path supplyChainPath + '/verification'
    And request defaultExpectedProducts
    When method POST
    Then status 403

  Scenario: SERVICE_ACCOUNT in other project verify
    * url karate.properties['server.baseurl']
    Given path '/api/nodes/'+defaultOrganizationId+'/projects'
    And request { name: 'project1', parentId: #(defaultOrganizationId)}
    When method POST
    Then status 201
    * def projectId = response.id
    Given path '/api/nodes/'+projectId+'/supplychains'
    And request { name: 'other', parentId: #(projectId)}
    When method POST
    Then status 201
    * def otherSupplyChainId = response.id
    * configure headers = call read('classpath:headers.js') { token: #(sa1.token)}
    Given path '/api/supplychains/'+ otherSupplyChainId + '/verification'
    And request defaultExpectedProducts
    When method POST
    Then status 403

