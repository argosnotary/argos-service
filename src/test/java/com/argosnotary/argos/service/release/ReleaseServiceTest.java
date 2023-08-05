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
package com.argosnotary.argos.service.release;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.argosnotary.argos.domain.crypto.PublicKey;
import com.argosnotary.argos.domain.layout.Layout;
import com.argosnotary.argos.domain.layout.LayoutMetaBlock;
import com.argosnotary.argos.domain.link.Artifact;
import com.argosnotary.argos.domain.nodes.Domain;
import com.argosnotary.argos.domain.nodes.Organization;
import com.argosnotary.argos.domain.release.Release;
import com.argosnotary.argos.domain.release.ReleaseResult;
import com.argosnotary.argos.service.account.AccountService;
import com.argosnotary.argos.service.layout.LayoutMetaBlockService;
import com.argosnotary.argos.service.link.LinkMetaBlockService;
import com.argosnotary.argos.service.mongodb.release.ReleaseDossierRepository;
import com.argosnotary.argos.service.mongodb.release.ReleaseRepository;
import com.argosnotary.argos.service.nodes.NodeService;
import com.argosnotary.argos.service.nodes.SupplyChainService;
import com.argosnotary.argos.service.verification.VerificationProvider;
import com.argosnotary.argos.service.verification.VerificationRunResult;

@ExtendWith(MockitoExtension.class)
class ReleaseServiceTest {
	private UUID SUPPLYCHAIN_ID = UUID.randomUUID();
	private String fullDomainName = "sc1.project1.org1.com";
	private ReleaseService releaseService;
	
	List<Set<Artifact>> releaseArtifacts;
	
	List<List<String>> releaseArtifactHashes;
	
	String releaseArtifactHashesHash;
	
	Artifact a11, a12, a21, a22, a23;
	
	Set<Artifact> allArtifacts;
	
	Release release;
	
	VerificationRunResult validVerificationRunResult;
	
	Organization org;
	
	@Mock
	private LayoutMetaBlock layoutMetaBlock;
	
	@Mock
	private Layout layout;

	@Mock
    private VerificationProvider verificationProvider;
	@Mock
    private LayoutMetaBlockService layoutMetaBlockService;
	@Mock
    private ReleaseRepository releaseRepository;
	@Mock
	private ReleaseDossierRepository releaseDossierRepository;
	@Mock
    private AccountService accountService;
	@Mock
    private SupplyChainService supplyChainService;
	@Mock
    private NodeService nodeService;
	@Mock
    private LinkMetaBlockService linkMetaBlockService;
	@Mock
    private PublicKey key;

	@BeforeEach
	void setUp() throws Exception {
		releaseService = new ReleaseServiceImpl(verificationProvider,layoutMetaBlockService,releaseRepository,releaseDossierRepository,accountService,nodeService, supplyChainService,linkMetaBlockService);
		a11 = new Artifact("hash11", "uri11");
		a12 = new Artifact("hash12", "uri12");
		a21 = new Artifact("hash21", "uri21");
		a22 = new Artifact("hash22", "uri22");
		a23 = new Artifact("hash23", "uri23");
		releaseArtifacts =  new ArrayList<>();
		releaseArtifacts.add(Set.of(a11,a12));
		releaseArtifacts.add(Set.of(a21,a22, a23));
		

		org = new Organization(UUID.randomUUID(), "org", Domain.builder().domain("org.com").build());
		
		releaseArtifactHashes = convertToReleaseArtifactHashes(releaseArtifacts);
		
		releaseArtifactHashesHash = Release.calculateReleasedProductsHashesHash(releaseArtifactHashes
				.stream()
				.flatMap(List::stream)
				.collect(Collectors.toSet()));
		
		allArtifacts = releaseArtifacts
                .stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
		
		Set<String> releasedProductsHashes = releaseArtifactHashes
				.stream()
				.flatMap(List::stream)
				.collect(Collectors.toSet());
		
		release = Release.builder()
				.domain(org.getDomain())
				.supplyChainId(SUPPLYCHAIN_ID)
				.releasedProductsHashes(releasedProductsHashes)
				.releasedProductsHashesHash(releaseArtifactHashesHash)
				.build();
		validVerificationRunResult = VerificationRunResult.builder().runIsValid(true).build();
	}
	
	@Test
	void testArtifactsAreTrusted() {
		when(releaseRepository.existsByDomainNamesAndHashes(List.of("org.com"), release.getReleasedProductsHashes())).thenReturn(true);
		assertTrue(releaseService.artifactsAreTrusted(release.getReleasedProductsHashes(), List.of("org.com")));
	}
	
	void testArtifactsAreNotTrusted() {
		Set<String> hashes = release.getReleasedProductsHashes();
		hashes.add("randomHash");
		when(releaseRepository.existsByDomainNamesAndHashes(List.of("org.com"), hashes)).thenReturn(true);
		assertFalse(releaseService.artifactsAreTrusted(release.getReleasedProductsHashes(), List.of("org.com")));
	}

	@Test
	void testCreateReleaseAvailable() {
		when(releaseRepository.findByReleasedProductsHashesHashAndSupplyChainId(releaseArtifactHashesHash, SUPPLYCHAIN_ID)).thenReturn(Optional.of(release));
		ReleaseResult res = releaseService.createRelease(SUPPLYCHAIN_ID, releaseArtifacts);
		assertEquals(release.getReleasedProductsHashesHash(), res.getRelease().getReleasedProductsHashesHash());
	}
	
	@Test
	void testCreateReleaseCreate() {
		when(nodeService.getQualifiedName(SUPPLYCHAIN_ID)).thenReturn(Optional.of(fullDomainName));
		when(releaseRepository.findByReleasedProductsHashesHashAndSupplyChainId(releaseArtifactHashesHash, SUPPLYCHAIN_ID)).thenReturn(Optional.empty());
		when(layoutMetaBlockService.getLayout(SUPPLYCHAIN_ID)).thenReturn(Optional.of(layoutMetaBlock));
		when(layoutMetaBlock.getLayout()).thenReturn(layout);
		when(layout.getKeys()).thenReturn(List.of(key));
		when(key.getKeyId()).thenReturn("keyId");
		when(verificationProvider.verifyRun(layoutMetaBlock, allArtifacts)).thenReturn(validVerificationRunResult);
		when(nodeService.findOrganizationInPath(SUPPLYCHAIN_ID)).thenReturn(org);
		when(releaseRepository.save(any())).thenReturn(release);
		ReleaseResult res = releaseService.createRelease(SUPPLYCHAIN_ID, releaseArtifacts);
		assertTrue(res.isReleaseIsValid());
		assertEquals(release.getDomain(), res.getRelease().getDomain());
		assertEquals(release.getQualifiedSupplyChainName(), res.getRelease().getQualifiedSupplyChainName());
		assertEquals(release.getReleasedProductsHashes(), res.getRelease().getReleasedProductsHashes());
		assertEquals(release.getReleasedProductsHashesHash(), res.getRelease().getReleasedProductsHashesHash());
		verify(linkMetaBlockService).deleteBySupplyChainId(SUPPLYCHAIN_ID);
		verify(releaseRepository).save(any());
	}
	
	private List<List<String>> convertToReleaseArtifactHashes(List<Set<Artifact>> releaseArtifacts) {
        return releaseArtifacts
                .stream()
                .map(s -> s.stream()
                        .map(Artifact::getHash)
                        .collect(Collectors.toList()))
                .collect(Collectors.toList());
    }

}
