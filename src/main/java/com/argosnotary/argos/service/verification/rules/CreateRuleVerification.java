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
package com.argosnotary.argos.service.verification.rules;

import com.argosnotary.argos.domain.layout.rule.Rule;
import com.argosnotary.argos.domain.layout.rule.RuleType;
import com.argosnotary.argos.domain.link.Artifact;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
@Slf4j
public class CreateRuleVerification implements RuleVerification {
    @Override
    public RuleType getRuleType() {
        return RuleType.CREATE;
    }

    @Override
    public boolean verify(RuleVerificationContext<? extends Rule> context) {
        Set<Artifact> filteredArtifacts = context.getFilteredArtifacts();
        
        Set<String> uris = context.getProducts().stream().map(Artifact::getUri).collect(Collectors.toSet());
        
        uris.removeAll(context.getMaterials().stream().map(Artifact::getUri).collect(Collectors.toSet()));
        
        if (filteredArtifacts.stream().map(Artifact::getUri).allMatch(uris::contains)) {
            context.consume(filteredArtifacts);
            logInfo(log, filteredArtifacts);
            return true;
        } else {
            logErrors(log, filteredArtifacts);
            return false;
        }
    }
}
