/*
 * Argos Notary - A new way to secure the Software Supply Chain
 *
 * Copyright (C) 2019 - 2020 Rabobank Nederland
 * Copyright (C) 2019 - 2025 Gerard Borst <gerard.borst@argosnotary.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
function fn(auth) {
    const token = auth.token;
    const username = auth.username;
    const password = auth.password;

    if (token) {
        return {
            Authorization: 'Bearer ' + token,
            'Content-Type': 'application/json'
        };
    } else if (username && password) {
        const temp = username + ':' + password;
        const Base64 = Java.type('java.util.Base64');
        const encoded = Base64.getEncoder().encodeToString(temp.bytes);
        return {
            Authorization: 'Basic ' + encoded,
            'Content-Type': 'application/json'
        };
    } else {
        return {};
    }
}