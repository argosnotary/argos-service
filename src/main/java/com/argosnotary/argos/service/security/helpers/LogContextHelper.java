/*
 * Argos Notary - A new way to secure the Software Supply Chain
 *
 * Copyright (C) 2019 - 2020 Rabobank Nederland
 * Copyright (C) 2019 - 2023 Gerard Borst <gerard.borst@argosnotary.com>
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
package com.argosnotary.argos.service.security.helpers;

import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import com.argosnotary.argos.service.account.ArgosUserDetails;

@Component
public class LogContextHelper {

    void addAccountInfoToLogContext(ArgosUserDetails userDetails) {
        MDC.put("accountId", userDetails.getAccount().getId().toString());
        MDC.put("accountName", userDetails.getAccount().getName());
    }

    void addTraceIdToLogContext() {
        MDC.put("traceId", UUID.randomUUID().toString());
    }
}
