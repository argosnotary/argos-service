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

import static com.argosnotary.argos.service.verification.Verification.Priority.STEP_AUTHORIZED_KEYID;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.argosnotary.argos.domain.link.LinkMetaBlock;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class StepAuthorizedKeyIdVerification implements Verification {

    @Override
    public Priority getPriority() {
        return STEP_AUTHORIZED_KEYID;
    }

    @Override
    public VerificationRunResult verify(VerificationContext context) {
        Map<String,List<String>> stepAndKeyIds = new HashMap<>();
        context
            .getLayoutMetaBlock()
            .getLayout()
            .getSteps().forEach(step -> stepAndKeyIds.put(step.getName(), step.getAuthorizedKeyIds()));

        List<LinkMetaBlock> failedLinkAuthorizedKeyIdVerifications = context
                .getLinkMetaBlocks()
                .stream()
                .filter(linkMetaBlock -> linkIsNotSignedByAuthorizedFunctionary(stepAndKeyIds, linkMetaBlock))
                .toList();

        if (!failedLinkAuthorizedKeyIdVerifications.isEmpty()) {
            failedLinkAuthorizedKeyIdVerifications
                .forEach(block -> log.info("LinkMetaBlock for step [{}] is signed with the not authorized key [{}] the linkMetaBlock will be removed from the context",
                        block.getLink().getStepName(), block.getSignature().getKeyId()));
            context.removeLinkMetaBlocks(failedLinkAuthorizedKeyIdVerifications);
        }

        return VerificationRunResult.okay();
    }

    private static boolean linkIsNotSignedByAuthorizedFunctionary(Map<String,List<String>> stepAndKeyIds, LinkMetaBlock linkMetaBlock) {
        return !stepAndKeyIds.get(linkMetaBlock.getLink().getStepName())
                .contains(linkMetaBlock.getSignature().getKeyId());
    }

}
