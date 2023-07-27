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

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.argosnotary.argos.domain.layout.LayoutMetaBlock;
import com.argosnotary.argos.domain.layout.rule.MatchRule;
import com.argosnotary.argos.domain.link.Artifact;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class VerificationProvider {

    private final List<Verification> verifications;

    private final VerificationContextsProvider verificationContextsProvider;

    @PostConstruct
    public void init() {
        verifications.sort(Comparator.comparing(Verification::getPriority));
        log.info("active verifications:");
        verifications.forEach(verification -> log.info("{} : {}", verification.getPriority(), verification.getClass().getSimpleName()));
    }

    public VerificationRunResult verifyRun(LayoutMetaBlock layoutMetaBlock, Set<Artifact> productsToVerify) {
    	
    	if (!expectedProductsComplete(layoutMetaBlock, productsToVerify)) {
    		return VerificationRunResult.valid(false);
    	}
        
        List<VerificationContext> possibleVerificationContexts = verificationContextsProvider
                .createPossibleVerificationContexts(layoutMetaBlock, productsToVerify);

        List<VerificationRunResult> verificationRunResults = possibleVerificationContexts
                .stream()
                .map(context -> verifications
                        .stream()
                        .map(verification -> verification.verify(context))
                        .filter(result -> !result.isRunIsValid())
                        .findFirst().orElse(VerificationRunResult
                                .builder()
                                .runIsValid(true)
                                .validLinkMetaBlocks(context.getOriginalLinkMetaBlocks())
                                .build())
                ).toList();

        return verificationRunResults
                .stream()
                .map(verificationRunResult -> {
                    log.info("context validity: {}", verificationRunResult.isRunIsValid());
                    return verificationRunResult;
                })
                .filter(VerificationRunResult::isRunIsValid)
                .findFirst().orElse(VerificationRunResult.valid(false));
    }
    
    /* Check if all expected end products Match Rules are used */
    private boolean expectedProductsComplete(LayoutMetaBlock layoutMetaBlock, Set<Artifact> productsToVerify) {
        List<MatchRule> rules = layoutMetaBlock.getLayout().getExpectedEndProducts();
        for (MatchRule rule: rules) {
            if (ArtifactsVerificationContext.filterArtifacts(new HashSet<>(productsToVerify), rule.getPattern(), rule.getSourcePathPrefix()).isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
