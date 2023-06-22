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
  Scenario: get logout should return 204
    Given path 'api/personalaccount/me/logout'
    And request ''
    When method PUT
    Then status 204
    * def expectedResponse = read('classpath:testmessages/personal-account/admin-account-response.json')
    Given path '/api/personalaccount/me'
    When method GET
    Then status 401


  Scenario: get account by id without ASSIGN_ROLE should return a 403
    * configure headers = call read('classpath:headers.js') { token: #(pa1.token)}
    Given path '/api/personalaccount/'+pa2.personalAccount.id
    When method GET
    Then status 403
    And match response == {"message":"Access denied"}

  Scenario: search personal account by active key id should return 200
    * def extraAccount = paLogin('user6')
    * def accountId = karate.toString(extraAccount.personalAccount.id)
    * def keyId = extraAccount.personalAccount.activeKeyPair.keyId
    * def expectedResponse = read('classpath:testmessages/personal-account/extra-account-identity-response.json')
    * configure headers = call read('classpath:headers.js') { token: #(pa1.token)}
    Given path '/api/personalaccount'
    And param activeKeyIds = keyId
    When method GET
    Then status 200
    And match response == [#(expectedResponse)]

  Scenario: search personal account by inactive key id should return 200
    * configure headers = call read('classpath:headers.js') { token: #(pa1.token)}
    Given path '/api/personalaccount'
    And param inactiveKeyIds = 'f808d5d02e2738467bc818d6c54ee68bcf8d13e78c3b1d4d50d73cbfc87fd447'
    When method GET
    Then status 200
    And match response == []

  Scenario: search all personal account 200
    * configure headers = call read('classpath:headers.js') { token: #(pa1.token)}
    * def expectedResponse = read('classpath:testmessages/personal-account/account-search-all-response.json')
    Given path '/api/personalaccount'
    When method GET
    Then status 200
    And match response == expectedResponse

  Scenario: search personal account without authentication should return a 401
    * configure headers = call read('classpath:headers.js') { token: ""}
    Given path '/api/personalaccount'
    When method GET
    Then status 401

  Scenario: search personal account by name should return 200
    * def extraAccount = paLogin('user6')
    * def accountId = karate.toString(extraAccount.personalAccount.id)
    * def expectedResponse = read('classpath:testmessages/personal-account/extra-account-identity-response.json')
    * configure headers = call read('classpath:headers.js') { token: #(pa1.token)}
    Given path '/api/personalaccount'
    And param name = 'user6'
    When method GET
    Then status 200
    And match response == [#(expectedResponse)]

  