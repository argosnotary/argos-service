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
package com.argosnotary.argos.service.verification.rules;

import com.argosnotary.argos.domain.link.Artifact;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;

@ExtendWith(MockitoExtension.class)
class RuleVerificationResultTest {

    @Mock
    private Artifact artifact;

    @Test
    void okay() {
        RuleVerificationResult ruleVerificationResult = RuleVerificationResult.okay(Set.of(artifact));
        assertThat(ruleVerificationResult.isValid(), is(true));
        assertThat(ruleVerificationResult.getValidatedArtifacts(), contains(artifact));
    }

    @Test
    void notOkay() {
        RuleVerificationResult ruleVerificationResult = RuleVerificationResult.notOkay();
        assertThat(ruleVerificationResult.isValid(), is(false));
        assertThat(ruleVerificationResult.getValidatedArtifacts(), empty());
    }
}
