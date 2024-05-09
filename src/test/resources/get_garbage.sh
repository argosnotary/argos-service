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

function exists_in_list() {
    LIST=$1
    DELIMITER=" "
    VALUE=$2
    [[ "$LIST" =~ ($DELIMITER|^)$VALUE($DELIMITER|$) ]]
}

script_dir=$(cd `dirname $0` && pwd)

cd ${script_dir}

features="get_garbage.sh feature/account/personalaccount.feature feature/account/search-account.feature feature/account/service-account.feature feature/hierarchy/hierarchy.feature feature/label/label.feature feature/layout/layout.feature feature/link/link.feature feature/oauthprovider/oauthprovider.feature feature/permission/permission.feature feature/release/release.feature feature/supplychain/supplychain.feature feature/verification/verification.feature feature/verification2.0/verification2.0.feature"

files=$(find . -type f| sort)

for f in ${files}; do
  cur_file=$(echo $f | sed 's/\.\///')
  if exists_in_list $features $cur_file; then
    continue
  fi
  if [[ $cur_file == testmessages/verification/* ]]; then
    test_string=$(echo $cur_file | cut -d"/" -f3)  
  else
    test_string=$cur_file
  fi
  count=$(grep -R "$test_string" * | wc -l)
  if [ $count -eq 0 ]; then
    echo "file $test_string $count times"
  fi
done
