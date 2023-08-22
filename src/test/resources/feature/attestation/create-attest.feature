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

@ignore
Feature: create a attestation

  Background:
    * url karate.properties['server.baseurl']
    * call read('classpath:common.feature')

  Scenario: store attestation with valid specifications should return a 204
    * def attestPath = '/api/supplychains/'+ supplyChainId + '/attest'
    * def attestToBeSigned = read(attestFile)
    * def attestation = call signAttestation { passphrase: #(signingAccount.passphrase), keyPair: #(signingAccount.serviceAccount.activeKeyPair), attestation: #(attestToBeSigned)}
    * configure headers = call read('classpath:headers.js') { token: #(signingAccount.token)}
    Given path attestPath
    And request attestation
    When method POST
    Then status 201