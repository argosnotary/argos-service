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
package com.argosnotary.argos.service.verification.rules;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.argosnotary.argos.domain.layout.rule.Rule;
import com.argosnotary.argos.domain.layout.rule.RuleType;
import com.argosnotary.argos.domain.link.Artifact;
import com.argosnotary.argos.service.verification.ArtifactsVerificationContext;

@ExtendWith(MockitoExtension.class)
class RequireRuleVerificationTest {

    private RequireRuleVerification verification;

    @Mock
    private RuleVerificationContext<? extends Rule> context;
    
    @Mock
    private ArtifactsVerificationContext artifactsContext;

    @Mock
    private Artifact artifact;

    @BeforeEach
    void setUp() {
        verification = new RequireRuleVerification();
    }

    @Test
    void getRuleType() {
        assertThat(verification.getRuleType(), is(RuleType.REQUIRE));
    }

    @Test
    void verifyArtifactsHappyFlow() {
        when(context.getFilteredArtifacts()).thenReturn(Set.of(artifact));
        assertThat(verification.verify(context), is(true));
        verify(context, times(1)).consume(Set.of(artifact));
    }

    @Test
    void verifyArtifactsProducts() {
        when(context.getFilteredArtifacts()).thenReturn(Set.of());
        
        assertThat(verification.verify(context), is(false));
        verify(context, times(0)).consume(anySet());
    }
}
