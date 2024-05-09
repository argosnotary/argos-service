#!/bin/bash
#
# Argos Notary - A new way to secure the Software Supply Chain
#
# Copyright (C) 2019 - 2020 Rabobank Nederland
# Copyright (C) 2019 - 2024 Gerard Borst <gerard.borst@argosnotary.com>
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


username=0895000d-1f79-4f1b-91e4-12c9061cdbd3

curl -s -X POST -H "Content-Type: application/x-www-form-urlencoded" \
   -d grant_type="password" \
   -d username=$username \
   -d password=test \
   -d scope=openid \
   -d client_id=saprovider-client \
   -d client_secret=644TyDbo7pTeyqDLM7kj4LWMwjRcUcBr \
   "http://localhost:9080/realms/saprovider/protocol/openid-connect/token?" | jq -r '.id_token'
   