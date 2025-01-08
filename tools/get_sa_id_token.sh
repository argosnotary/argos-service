#!/bin/bash
#
# Argos Notary - A new way to secure the Software Supply Chain
#
# Copyright (C) 2019 - 2020 Rabobank Nederland
# Copyright (C) 2019 - 2025 Gerard Borst <gerard.borst@argosnotary.com>
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

curl -X POST -H "Content-Type: application/json" \
   -d '{"accountId": "0895000d-1f79-4f1b-91e4-12c9061cdbd3", "passphrase": "test"}' \
   "http://localhost:8080/api/serviceaccounts/me/token"