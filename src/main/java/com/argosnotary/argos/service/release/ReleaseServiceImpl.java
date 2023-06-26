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
import com.argosnotary.argos.domain.nodes.Organization;
import com.argosnotary.argos.domain.release.Release;
import com.argosnotary.argos.domain.release.ReleaseDossier;
import com.argosnotary.argos.domain.release.ReleaseResult;
import com.argosnotary.argos.service.account.AccountService;
import com.argosnotary.argos.service.layout.LayoutMetaBlockService;
import com.argosnotary.argos.service.link.LinkMetaBlockService;
import com.argosnotary.argos.service.mongodb.release.ReleaseDossierRepository;
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
    private final ReleaseDossierRepository releaseDossierRepository;
    private final AccountService accountService;
    private final SupplyChainService supplyChainService;
    private final LinkMetaBlockService linkMetaBlockService;

    @Override
    public ReleaseResult createRelease(UUID supplyChainId, List<Set<Artifact>> releaseArtifacts) {
        log.info("Release Artifacts [{}] for supply chain [{}].", releaseArtifacts, supplyChainId);
        String releaseArtifactHashesHash = convertToReleaseArtifactHashesHash(releaseArtifacts);
        return releaseRepository
        		.findByReleasedProductsHashesHashAndSupplyChainId(releaseArtifactHashesHash, supplyChainId)
                .map(release -> {
                    log.info("Artifacts already released [{}] for supply chain [{}].", releaseArtifacts, supplyChainId);
                    return ReleaseResult
                        .builder()
                        .releaseIsValid(true)
                        .release(release)
                        .build();
                })
                .orElseGet(() -> verifyAndStoreRelease(supplyChainId, releaseArtifacts));
    }

    private ReleaseResult verifyAndStoreRelease(UUID supplyChainId, 
    		List<Set<Artifact>> releaseArtifacts) {
        ReleaseResult.ReleaseResultBuilder releaseBuilder = ReleaseResult.builder();
        Optional<LayoutMetaBlock> optionalLayoutMetaBlock = layoutMetaBlockService.findBySupplyChainId(supplyChainId);
        if (optionalLayoutMetaBlock.isPresent()) {

            Set<Artifact> allArtifacts = releaseArtifacts
                    .stream()
                    .flatMap(Collection::stream)
                    .collect(Collectors.toSet());

            VerificationRunResult verificationRunResult = verificationProvider.verifyRun(optionalLayoutMetaBlock.get(), allArtifacts);
            releaseBuilder.releaseIsValid(verificationRunResult.isRunIsValid());
            
            Optional<Organization> orgOpt = supplyChainService.getOrganization(supplyChainId);
            if (orgOpt.isEmpty()) {
            	log.info("Artifacts release invalid [{}] for supply chain [{}].", releaseArtifacts, supplyChainId);
            	log.info("Organization for supply chain [{}] not found.", supplyChainId);
                return ReleaseResult.builder().releaseIsValid(false).build();
            }
            
            Optional<String> qualifiedNameOpt = supplyChainService.getQualifiedName(supplyChainId);
            if (qualifiedNameOpt.isEmpty()) {
            	log.info("Artifacts release invalid [{}] for supply chain [{}].", releaseArtifacts, supplyChainId);
                return ReleaseResult.builder().releaseIsValid(false).build();
            }
            

            OffsetDateTime releaseDate = OffsetDateTime.now(ZoneOffset.UTC);

            String releaseArtifactHashesHash = convertToReleaseArtifactHashesHash(releaseArtifacts);
            
            String releaseName =  qualifiedNameOpt.get()+ "-" + releaseDate.toInstant().getEpochSecond();

            if (verificationRunResult.isRunIsValid()) {
                Release release = Release.builder()
                		.id(UUID.randomUUID())
                		.name(releaseName)
                		.supplyChainId(supplyChainId)
                		.qualifiedSupplyChainName(qualifiedNameOpt.get())
                		.organization(orgOpt.get())
                		.releaseDate(releaseDate)
                		.releasedProductsHashes(convertToReleaseArtifactHashes(releaseArtifacts))
                		.releasedProductsHashesHash(releaseArtifactHashesHash)
                		.build();
                Release released = createAndStoreRelease(
                				release,
                        optionalLayoutMetaBlock.get(),
                        verificationRunResult);
                releaseBuilder.release(released);
                linkMetaBlockService.deleteBySupplyChainId(supplyChainId);
            }
            log.info("Artifacts released [{}] for supply chain [{}].", releaseArtifacts, supplyChainId);
            return releaseBuilder.build();
        }
        log.info("Artifacts release invalid [{}] for supply chain [{}].", releaseArtifacts, supplyChainId);
        return ReleaseResult.builder().releaseIsValid(false).build();
    }

    private Release createAndStoreRelease(Release release, LayoutMetaBlock layoutMetaBlock,
                                                         VerificationRunResult verificationRunResult) {

        List<Account> accounts = getAccounts(layoutMetaBlock);

        ReleaseDossier releaseDossier = ReleaseDossier.builder()
                .layoutMetaBlock(layoutMetaBlock)
                .linkMetaBlocks(verificationRunResult.getValidLinkMetaBlocks())
                .accounts(accounts)
                .build();
        Release rel = releaseDossierRepository.storeRelease(release, releaseDossier);
        return releaseRepository.save(rel);
    }

    private Set<String> convertToReleaseArtifactHashes(List<Set<Artifact>> releaseArtifacts) {
        return releaseArtifacts
                .stream()
                .flatMap(a -> a.stream())
                .map(Artifact::getHash)
                .collect(Collectors.toSet());
    }
    
    private String convertToReleaseArtifactHashesHash(List<Set<Artifact>> releaseArtifacts) {
    	Set<String> hashes = convertToReleaseArtifactHashes(releaseArtifacts);
        return Release.calculateReleasedProductsHashesHash(hashes);
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
	public boolean artifactsAreReleased(Set<String> artifacts, List<String> domains) {
		return releaseRepository.artifactsNotReleased(domains, artifacts).getReleasedProductsHashes().isEmpty();
	}
}
