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
package com.argosnotary.argos.service.rest.verification;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import com.argosnotary.argos.service.openapi.rest.model.RestVerificationResult;
import com.argosnotary.argos.service.verification.VerificationRunResult;

class VerificationResultMapperTest {

    VerificationResultMapper verificationResultMapper;

    @BeforeEach
    public void setup() {
        verificationResultMapper = Mappers.getMapper(VerificationResultMapper.class);
    }

    @Test
    void mapToRestVerificationResultShouldReturnResult() {
        VerificationRunResult restVerificationResult = VerificationRunResult.okay();
        RestVerificationResult result = verificationResultMapper.mapToRestVerificationResult(restVerificationResult);
        assertThat(result.getRunIsValid(), is(true));
    }
}