/*
 * Argos Notary - A new way to secure the Software Supply Chain
 *
 * Copyright (C) 2019 - 2020 Rabobank Nederland
 * Copyright (C) 2019 - 2024 Gerard Borst <gerard.borst@argosnotary.com>
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesPattern;

import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

import com.argosnotary.argos.domain.account.PersonalAccount;
import com.argosnotary.argos.service.account.ArgosUserDetails;

@ExtendWith(MockitoExtension.class)
class LogContextHelperTest {
	private static final UUID ACCOUNT_ID = UUID.randomUUID();
    private static final String ACCOUNT_NAME = "accountName";
    
    private ArgosUserDetails accountUserDetailsAdapter;
    
    private PersonalAccount account;


    private LogContextHelper logContextHelper;

    @BeforeEach
    void setup() {
        logContextHelper = new LogContextHelper();
        account = PersonalAccount.builder().id(ACCOUNT_ID).name(ACCOUNT_NAME).build();
        accountUserDetailsAdapter = new ArgosUserDetails(account);


    }

    @Test
    void addAccountInfoToLogContext() {
        logContextHelper.addAccountInfoToLogContext(accountUserDetailsAdapter);
        assertThat(MDC.get("accountId"), is(ACCOUNT_ID.toString()));
        assertThat(MDC.get(ACCOUNT_NAME), is(ACCOUNT_NAME));
    }

    @Test
    void addTraceIdToLogContext() {
        logContextHelper.addTraceIdToLogContext();
        String traceId = MDC.get("traceId");
        assertThat(traceId, matchesPattern("[0-9a-fA-F]{8}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{12}"));
    }

    @AfterEach
    void removeFromMDC() {
        MDC.clear();
    }
}