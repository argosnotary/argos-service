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

import com.argosnotary.argos.domain.layout.rule.Rule;
import com.argosnotary.argos.domain.layout.rule.RuleType;
import com.argosnotary.argos.domain.link.Artifact;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
@Slf4j
public class DeleteRuleVerification implements RuleVerification {
    @Override
    public RuleType getRuleType() {
        return RuleType.DELETE;
    }

    @Override
    public boolean verify(RuleVerificationContext<? extends Rule> context) {
        // deleteRule filteredMaterials must not be in filteredProducts
        // example pattern **/*.java not in filteredProducts but exists in filteredMaterials
        Set<Artifact> filteredArtifacts = context.getFilteredArtifacts();
        Set<Artifact> complement = new HashSet<>(context.getMaterials());
        complement.removeAll(context.getProducts());
        if (filteredArtifacts.stream().allMatch(complement::contains)) {
            context.consume(filteredArtifacts);
            logInfo(log, filteredArtifacts);
            return true;
        } else {
            logErrors(log, filteredArtifacts);
            return false;
        }
    }
}
