/*
 * Argos Notary - A new way to secure the Software Supply Chain
 *
 * Copyright (C) 2019 - 2020 Rabobank Nederland
 * Copyright (C) 2019 - 2022 Gerard Borst <gerard.borst@argosnotary.com>
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
package com.argosnotary.argos.service.verification;

import static com.argosnotary.argos.service.verification.Verification.Priority.REQUIRED_NUMBER_OF_LINKS;
import static java.util.stream.Collectors.groupingBy;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.argosnotary.argos.domain.layout.Step;
import com.argosnotary.argos.domain.link.LinkMetaBlock;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class RequiredNumberOfLinksVerification implements Verification {
    @Override
    public Priority getPriority() {
        return REQUIRED_NUMBER_OF_LINKS;
    }

    @Override
    public VerificationRunResult verify(VerificationContext context) {

        return context.getLayoutMetaBlock().getLayout().getSteps()
                .stream()
                .filter(step -> !isValid(step, context))
                .findFirst()
                .map(segment -> VerificationRunResult.builder().verification(this.getPriority()).runIsValid(false).build())
                .orElse(VerificationRunResult.okay());

    }

    private Boolean isValid(Step step, VerificationContext context) {
        Map<String, Set<LinkMetaBlock>> stepLinkMetaBlockMap = context.getStepNameLinkMetaBlockMap();
        
        Map<Integer, Set<LinkMetaBlock>> linkMetaBlockMap = stepLinkMetaBlockMap
                .get(step.getName()).stream()
                .collect(groupingBy(f -> f.getLink().hashCode(), Collectors.toSet()));
        if (linkMetaBlockMap.size() == 1) {
            return isValid(linkMetaBlockMap.values().iterator().next(), step);
        } else {
            log.info("[{}] different link objects in metablocks for step [{}]", linkMetaBlockMap.size(), step);
            return false;
        }
    }

    private boolean isValid(Set<LinkMetaBlock> linkMetaBlocks, Step step) {
        log.info("[{}] links for step [{}] and should be at least [{}]", linkMetaBlocks.size(), step.getName(), step.getRequiredNumberOfLinks());
        return linkMetaBlocks.size() >= step.getRequiredNumberOfLinks();
    }
}
