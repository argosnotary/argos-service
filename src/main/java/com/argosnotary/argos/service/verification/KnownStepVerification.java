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
package com.argosnotary.argos.service.verification;

import static com.argosnotary.argos.service.verification.Verification.Priority.KNOWN_STEP;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.argosnotary.argos.domain.link.LinkMetaBlock;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class KnownStepVerification implements Verification {

    @Override
    public Priority getPriority() {
        return KNOWN_STEP;
    }

    @Override
    public VerificationRunResult verify(VerificationContext context) {
        Set<String> knownStepNames = new HashSet<>();
        context
            .getLayoutMetaBlock()
            .getLayout()
            .getSteps().forEach(step -> knownStepNames.add(step.getName()));

        List<LinkMetaBlock> failedStepNameVerifications = context
                .getLinkMetaBlocks()
                .stream()
                .filter(linkMetaBlock -> !knownStepNames.contains(linkMetaBlock.getLink().getStepName()))
                .toList();

        if (!failedStepNameVerifications.isEmpty()) {
        	failedStepNameVerifications
                .forEach(block -> log.info("LinkMetaBlock has a not known step [{}], the linkMetaBlock will be removed from the context",
                        block.getLink().getStepName()));
            context.removeLinkMetaBlocks(failedStepNameVerifications);
        }

        return VerificationRunResult.okay();
    }
}
