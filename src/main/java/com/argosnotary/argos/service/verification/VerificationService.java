package com.argosnotary.argos.service.verification;

import java.util.List;
import java.util.Set;

import com.argosnotary.argos.domain.layout.LayoutMetaBlock;
import com.argosnotary.argos.domain.link.Artifact;

public interface VerificationService {
	public boolean getVerification(List<String> artifactHashes, List<String> paths);
	
	public VerificationRunResult performVerification(LayoutMetaBlock layoutMetaBlock, Set<Artifact> expectedProducts);

}
