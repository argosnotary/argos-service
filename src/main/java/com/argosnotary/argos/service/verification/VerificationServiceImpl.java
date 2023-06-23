package com.argosnotary.argos.service.verification;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.argosnotary.argos.domain.layout.LayoutMetaBlock;
import com.argosnotary.argos.domain.link.Artifact;
import com.argosnotary.argos.service.release.ReleaseService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class VerificationServiceImpl implements VerificationService {

    private final VerificationProvider verificationProvider;

    private final ReleaseService releaseService;

	@Override
	public boolean getVerification(List<String> artifactHashes, List<String> paths) {
        return releaseService.artifactsAreReleased(artifactHashes.stream().collect(Collectors.toSet()), paths);
	}

	@Override
	public VerificationRunResult performVerification(LayoutMetaBlock layoutMetaBlock, Set<Artifact> expectedProduct) {
        return verificationProvider.verifyRun(layoutMetaBlock, expectedProduct);
	}
}
