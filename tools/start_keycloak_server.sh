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

set -e

keycloak_version=26.0

save_dir=$PWD
script_dir=$(dirname "$0")

cd ${script_dir}

docker run --name mykeycloak -p 8080:8080 -p 9000:9000 \
   -v $PWD/../src/test/resources/dev/keycloak/import:/opt/keycloak/data/import \
   --rm \
   quay.io/keycloak/keycloak:${keycloak_version} \
      start-dev \
         -Dkeycloak.migration.action=import \
         -Dkeycloak.migration.provider=dir \
         -Dkeycloak.migration.dir=/opt/keycloak/data/import
        
cd ${save_dir}