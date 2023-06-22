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
package com.argosnotary.argos.service.release;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.argosnotary.argos.domain.account.Account;
import com.argosnotary.argos.domain.crypto.PublicKey;
import com.argosnotary.argos.domain.layout.LayoutMetaBlock;
import com.argosnotary.argos.domain.link.Artifact;
import com.argosnotary.argos.domain.release.ReleaseDossier;
import com.argosnotary.argos.domain.release.ReleaseDossierMetaData;
import com.argosnotary.argos.domain.release.ReleaseResult;
import com.argosnotary.argos.service.account.AccountService;
import com.argosnotary.argos.service.layout.LayoutMetaBlockService;
import com.argosnotary.argos.service.link.LinkMetaBlockService;
import com.argosnotary.argos.service.mongodb.release.ReleaseRepository;
import com.argosnotary.argos.service.nodes.SupplyChainService;
import com.argosnotary.argos.service.verification.VerificationProvider;
import com.argosnotary.argos.service.verification.VerificationRunResult;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReleaseServiceImpl implements ReleaseService {

    private final VerificationProvider verificationProvider;
    private final LayoutMetaBlockService layoutMetaBlockService;
    private final ReleaseRepository releaseRepository;
    private final AccountService accountService;
    private final SupplyChainService supplyChainService;
    private final LinkMetaBlockService linkMetaBlockService;

    @Override
    public ReleaseResult createRelease(UUID supplyChainId, List<Set<Artifact>> releaseArtifacts) {
        log.info("Release Artifacts [{}] for supply chain [{}].", releaseArtifacts, supplyChainId);

        Optional<String> supplyChainPathOpt = supplyChainService.getFullDomainName(supplyChainId);
        if (supplyChainPathOpt.isEmpty()) {
        	return ReleaseResult.builder().releaseIsValid(false).build();
        }
        String supplyChainPath = supplyChainPathOpt.get();
        List<List<String>> releaseArtifactHashes = convertToReleaseArtifactHashes(releaseArtifacts);
        return releaseRepository
                .findReleaseByReleasedArtifactsAndPath(releaseArtifactHashes, supplyChainPath)
                .map(releaseDossierMetaData -> {
                    log.info("Artifacts already released [{}] for supply chain [{}].", releaseArtifacts, supplyChainId);
                    return ReleaseResult
                        .builder()
                        .releaseIsValid(true)
                        .releaseDossierMetaData(releaseDossierMetaData)
                        .build();
                })
                .orElseGet(() -> verifyAndStoreRelease(supplyChainId, releaseArtifacts, supplyChainPath, releaseArtifactHashes));
    }

    private ReleaseResult verifyAndStoreRelease(UUID supplyChainId, List<Set<Artifact>> releaseArtifacts, String supplyChainPath, List<List<String>> releaseArtifactHashes) {
        ReleaseResult.ReleaseResultBuilder releaseBuilder = ReleaseResult.builder();
        Optional<LayoutMetaBlock> optionalLayoutMetaBlock = layoutMetaBlockService.findBySupplyChainId(supplyChainId);
        if (optionalLayoutMetaBlock.isPresent()) {

            Set<Artifact> allArtifacts = releaseArtifacts
                    .stream()
                    .flatMap(Collection::stream)
                    .collect(Collectors.toSet());

            VerificationRunResult verificationRunResult = verificationProvider.verifyRun(optionalLayoutMetaBlock.get(), allArtifacts);
            releaseBuilder.releaseIsValid(verificationRunResult.isRunIsValid());

            if (verificationRunResult.isRunIsValid()) {
                ReleaseDossierMetaData releaseDossierMetaData = createAndStoreRelease(
                        supplyChainPath,
                        optionalLayoutMetaBlock.get(),
                        verificationRunResult,
                        releaseArtifactHashes);
                releaseBuilder.releaseDossierMetaData(releaseDossierMetaData);
                linkMetaBlockService.deleteBySupplyChainId(supplyChainId);
            }
            log.info("Artifacts released [{}] for supply chain [{}].", releaseArtifacts, supplyChainId);
            return releaseBuilder.build();
        }
        log.info("Artifacts release invalid [{}] for supply chain [{}].", releaseArtifacts, supplyChainId);
        return ReleaseResult.builder().releaseIsValid(false).build();
    }

    private ReleaseDossierMetaData createAndStoreRelease(String supplyChainPath, LayoutMetaBlock layoutMetaBlock,
                                                         VerificationRunResult verificationRunResult,
                                                         List<List<String>> releaseArtifacts) {

        List<Account> accounts = getAccounts(layoutMetaBlock);

        ReleaseDossierMetaData releaseDossierMetaData = ReleaseDossierMetaData.builder()
                .releaseArtifacts(releaseArtifacts)
                .releaseDate(OffsetDateTime.now(ZoneOffset.UTC))
                .supplyChainPath(supplyChainPath)
                .build();

        ReleaseDossier releaseDossier = ReleaseDossier.builder()
                .layoutMetaBlock(layoutMetaBlock)
                .linkMetaBlocks(verificationRunResult.getValidLinkMetaBlocks())
                .accounts(accounts)
                .build();
        ReleaseDossierMetaData rmd = releaseRepository.storeRelease(releaseDossierMetaData, releaseDossier);
        return rmd;
    }

    private List<List<String>> convertToReleaseArtifactHashes(List<Set<Artifact>> releaseArtifacts) {
        return releaseArtifacts
                .stream()
                .map(s -> s.stream()
                        .map(Artifact::getHash)
                        .collect(Collectors.toList()))
                .collect(Collectors.toList());
    }

    private List<Account> getAccounts(LayoutMetaBlock layoutMetaBlock) {
        Set<String> keyIds = layoutMetaBlock
                .getLayout()
                .getKeys()
                .stream()
                .map(PublicKey::getKeyId)
                .collect(Collectors.toSet());

        return accountService.findByKeyIds(keyIds);
    }

	@Override
	public Optional<String> getRawReleaseFileById(String id) {
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public boolean artifactsAreReleased(List<String> releasedArtifacts, List<String> paths) {
		// TODO Auto-generated method stub
		return false;
	}
}
