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
Feature: create a valid link

  Background:
    * url karate.properties['server.baseurl']
    * call read('classpath:common.feature')

  Scenario: store link with valid specifications should return a 204
    * def linkToBeSigned = read(linkFile)
    * def signedLink = call signLink { passphrase: #(signingAccount.passphrase), keyPair: #(signingAccount.serviceAccount.activeKeyPair), linkMetaBlock: #(linkToBeSigned)}
    * configure headers = call read('classpath:headers.js') { token: #(signingAccount.token)}
    Given path linkPath
    And request signedLink
    When method POST
    Then status 204