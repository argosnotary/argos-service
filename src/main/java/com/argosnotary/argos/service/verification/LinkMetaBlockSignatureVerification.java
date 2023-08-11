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

import static com.argosnotary.argos.service.verification.Verification.Priority.LINK_METABLOCK_SIGNATURE;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.argosnotary.argos.domain.crypto.PublicKey;
import com.argosnotary.argos.domain.layout.LayoutMetaBlock;
import com.argosnotary.argos.domain.link.LinkMetaBlock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Component
@RequiredArgsConstructor
@Slf4j
public class LinkMetaBlockSignatureVerification implements Verification {
	
	private final SignatureValidatorService signatureValidatorService;

    @Override
    public Priority getPriority() {
        return LINK_METABLOCK_SIGNATURE;
    }

    @Override
    public VerificationRunResult verify(VerificationContext context) {
        context.removeLinkMetaBlocks(context.getLinkMetaBlocks().stream()
                .filter(linkMetaBlock -> !okay(context.getLayoutMetaBlock(), linkMetaBlock)).toList());
        return VerificationRunResult.okay();
    }

    private boolean okay(LayoutMetaBlock layoutMetaBlock, LinkMetaBlock linkMetaBlock) {
        return signatureValidatorService.validateSignature(linkMetaBlock.getLink(), linkMetaBlock.getSignature(), getPublicKeyById(layoutMetaBlock, linkMetaBlock.getSignature().getKeyId()));
    }

    private Optional<PublicKey> getPublicKeyById(LayoutMetaBlock layoutMetaBlock, String keyId) {
        return layoutMetaBlock.getLayout().getKeys().stream().filter(publicKey -> publicKey.getKeyId().equals(keyId)).findFirst();
    }

}
