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

import static com.argosnotary.argos.service.verification.Verification.Priority.LAYOUT_METABLOCK_SIGNATURE;

import org.springframework.stereotype.Component;

import com.argosnotary.argos.domain.layout.LayoutMetaBlock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class LayoutMetaBlockSignatureVerification implements Verification {
	
	private final SignatureValidatorService signatureValidatorService;

    @Override
    public Priority getPriority() {
        return LAYOUT_METABLOCK_SIGNATURE;
    }

    @Override
    public VerificationRunResult verify(VerificationContext context) {
        return verify(context.getLayoutMetaBlock());
    }

    private VerificationRunResult verify(LayoutMetaBlock layoutMetaBlock) {
        boolean isValid = layoutMetaBlock
                .getSignatures()
                .stream()
                .allMatch(signature -> signatureValidatorService.validateSignature(layoutMetaBlock.getLayout(), signature));
                        
        if (!isValid) {
            log.info("failed LayoutMetaBlockSignatureVerification");
        }
        return VerificationRunResult.builder()
                .runIsValid(isValid)
                .build();

    }
}
