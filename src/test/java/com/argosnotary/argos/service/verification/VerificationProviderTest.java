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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.util.io.pem.PemGenerationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.argosnotary.argos.domain.crypto.CryptoHelper;
import com.argosnotary.argos.domain.crypto.KeyPair;
import com.argosnotary.argos.domain.crypto.Signature;
import com.argosnotary.argos.domain.crypto.signing.JsonSigningSerializer;
import com.argosnotary.argos.domain.layout.ArtifactType;
import com.argosnotary.argos.domain.layout.Layout;
import com.argosnotary.argos.domain.layout.LayoutMetaBlock;
import com.argosnotary.argos.domain.layout.Step;
import com.argosnotary.argos.domain.layout.rule.MatchRule;
import com.argosnotary.argos.domain.layout.rule.Rule;
import com.argosnotary.argos.domain.layout.rule.RuleType;
import com.argosnotary.argos.domain.link.Artifact;
import com.argosnotary.argos.domain.link.Link;
import com.argosnotary.argos.domain.link.LinkMetaBlock;
import com.argosnotary.argos.service.account.AccountService;
import com.argosnotary.argos.service.mongodb.link.LinkMetaBlockRepository;
import com.argosnotary.argos.service.verification.rules.AllowRuleVerification;
import com.argosnotary.argos.service.verification.rules.CreateOrModifyRuleVerification;
import com.argosnotary.argos.service.verification.rules.CreateRuleVerification;
import com.argosnotary.argos.service.verification.rules.DeleteRuleVerification;
import com.argosnotary.argos.service.verification.rules.DisallowRuleVerification;
import com.argosnotary.argos.service.verification.rules.MatchRuleVerification;
import com.argosnotary.argos.service.verification.rules.ModifyRuleVerification;
import com.argosnotary.argos.service.verification.rules.RequireRuleVerification;
import com.argosnotary.argos.service.verification.rules.RuleVerification;

@ExtendWith(MockitoExtension.class)
class VerificationProviderTest {
	private static final UUID SUPPLY_CHAIN_ID = UUID.randomUUID();
	private static final char[] PASSWORD = "wachtwoord".toCharArray();
	
	@Mock
	LinkMetaBlockRepository linkMetaBlockRepository;
    
    @Mock
    private AccountService accountService;
    
    private SignatureValidatorService signatureValidatorService;

    private List<RuleVerification> ruleVerificationList;

	VerificationContextsProvider verificationContextsProvider;
	
	private List<Verification> verifications;

    private LayoutMetaBlock layoutMetaBlock;

    private LinkMetaBlock buildStepLink, testStepLink;
    
    private KeyPair kp1, kp2, kp3;
    
    JsonSigningSerializer jsonSigningSerializer = new JsonSigningSerializer();

    private VerificationProvider verificationProvider;

    @BeforeEach
    void setup() throws NoSuchAlgorithmException, OperatorCreationException, PemGenerationException {
    	signatureValidatorService = new SignatureValidatorService(accountService);
    	kp1 = CryptoHelper.createKeyPair(PASSWORD);
    	kp2 = CryptoHelper.createKeyPair(PASSWORD);
    	kp3 = CryptoHelper.createKeyPair(PASSWORD);
    	Layout layout = Layout.builder()
    			.keys(List.of(kp1,kp2,kp3))
    			.authorizedKeyIds(List.of(kp1.getKeyId()))
    			.expectedEndProducts(List.of(MatchRule.builder()
    					.pattern("target/argos-test-0.0.1-SNAPSHOT.jar")
    					.destinationType(ArtifactType.PRODUCTS)
    					.destinationStepName("build")
    					.build()))
    			.steps(List.of(
    					Step.builder()
    					.name("build")
    					.authorizedKeyIds(List.of(kp2.getKeyId()))
    					.requiredNumberOfLinks(1)
    					.expectedMaterials(List.of(
    							new Rule(RuleType.REQUIRE, "**Argos4jIT.java"), 
    							new Rule(RuleType.ALLOW, "**")))
    					.expectedProducts(List.of(
    							new Rule(RuleType.MODIFY, "target/maven-archiver/pom.properties"), 
    							new Rule(RuleType.CREATE, "target/argos-test-0.0.1-SNAPSHOT.jar"), 
    							new Rule(RuleType.DELETE, "**/*.java"), 
    							new Rule(RuleType.ALLOW, "**"))
    							)
    					.build(),
    					Step.builder()
    					.name("test")
    					.authorizedKeyIds(List.of(kp3.getKeyId()))
    					.requiredNumberOfLinks(1)
    					.expectedMaterials(List.of(
    							MatchRule.builder()
    							.pattern("pom.xml")
    							.destinationType(ArtifactType.MATERIALS)
    							.destinationStepName("build")
    							.build(),
    							MatchRule.builder()
    							.pattern("target/argos-test-0.0.1-SNAPSHOT.jar")
    							.destinationType(ArtifactType.PRODUCTS)
    							.destinationStepName("build")
    							.build(), 
    							new Rule(RuleType.DISALLOW, "**/invalid-link.json"), 
    							new Rule(RuleType.ALLOW, "src/**")))
    					.expectedProducts(List.of(
    							MatchRule.builder()
    							.pattern("target/argos-test-0.0.1-SNAPSHOT.jar")
    							.destinationType(ArtifactType.PRODUCTS)
    							.destinationStepName("build")
    							.build(),
    							new Rule(RuleType.REQUIRE, "**Argos4jIT.java"),
    							new Rule(RuleType.DISALLOW, "**/bob"), 
    							new Rule(RuleType.ALLOW, "src/**"),
    							new Rule(RuleType.ALLOW, "*"))
    							)
    					.build()
    					))
    			.build();

    	Signature sig = CryptoHelper.sign(kp1, PASSWORD, jsonSigningSerializer.serialize(layout));
    	layoutMetaBlock = LayoutMetaBlock.builder().layout(layout).supplyChainId(SUPPLY_CHAIN_ID).signatures(List.of(sig)).build();
    	ruleVerificationList = new ArrayList<>(List.of(new AllowRuleVerification(), 
    			new CreateOrModifyRuleVerification(), 
    			new CreateRuleVerification(), 
    			new DeleteRuleVerification(), 
    			new DisallowRuleVerification(), 
    			new MatchRuleVerification(), 
    			new ModifyRuleVerification(), 
    			new RequireRuleVerification()));
    	RulesVerification ruleVer = new RulesVerification(ruleVerificationList);
    	ruleVer.init();
        verifications = new ArrayList<>(List.of(
        		new LayoutAuthorizedKeyIdVerification(), 
        		new LayoutMetaBlockSignatureVerification(signatureValidatorService), 
        		new KnownStepVerification(), 
        		new StepAuthorizedKeyIdVerification(), 
        		new LinkMetaBlockSignatureVerification(signatureValidatorService), 
        		new RequiredNumberOfLinksVerification(), 
        		ruleVer,
        		new ExpectedEndProductsVerification()));
    	verificationContextsProvider = new VerificationContextsProvider(linkMetaBlockRepository, ruleVerificationList);
    	verificationContextsProvider.init();
        verificationProvider = new VerificationProvider(verifications, verificationContextsProvider);
        verificationProvider.init();
        Link link = Link.builder()
        		.stepName("build")
        		.materials(List.of(
        				new Artifact("pom.xml", "df272f57191b2d45b55e04c8602194d724e485aefc77827ceba798352cb2bd77"),
        				new Artifact("src/test/java/com/argosnotary/argos/test/Argos4jIT.java", "ec792b16cd2b2446011acd992f6d8224862367373b0a85b1fc3bea890587f646"),
        				new Artifact("target/maven-archiver/pom.properties","d3a238f87bef515a65b8a99f5d25367282e2be12c62903b9f5d230e0491331f3")))
        		.products(List.of(
        				new Artifact("target/argos-test-0.0.1-SNAPSHOT.jar","49e73a11c5e689db448d866ce08848ac5886cac8aa31156ea4de37427aca6162"),
        		        new Artifact("target/maven-archiver/pom.properties","d3a238f87bef515a65b8a99f5d25367282e2be12c62903b9f5d230e0491331f2")))
        		.build();
        sig = CryptoHelper.sign(kp2, PASSWORD, jsonSigningSerializer.serialize(link));
        buildStepLink = LinkMetaBlock.builder()
        		.link(link)
        		.signature(sig)
        		.supplyChainId(SUPPLY_CHAIN_ID)
        		.build();
        
        link = Link.builder()
        		.stepName("test")
        		.materials(List.of(
        				new Artifact("pom.xml", "df272f57191b2d45b55e04c8602194d724e485aefc77827ceba798352cb2bd77"),
        				new Artifact("src/test/java/com/argosnotary/argos/test/Argos4jIT.java", "ec792b16cd2b2446011acd992f6d8224862367373b0a85b1fc3bea890587f646"),
        				new Artifact("src/test/resources/bob", "9484e07b40a0d9c279e33054fa976e4ae5de6191b29e96ef4ce1d13af7626b34"),
        				new Artifact("target/argos-test-0.0.1-SNAPSHOT.jar","49e73a11c5e689db448d866ce08848ac5886cac8aa31156ea4de37427aca6162")))
        		.products(List.of(
        				new Artifact("pom.xml", "df272f57191b2d45b55e04c8602194d724e485aefc77827ceba798352cb2bd77"),
        				new Artifact("src/test/java/com/argosnotary/argos/test/Argos4jIT.java", "ec792b16cd2b2446011acd992f6d8224862367373b0a85b1fc3bea890587f646"),
        				new Artifact("src/test/resources/testmessages/invalid-link.json", "0a7f22930c56c897b987ad4dc45d541ff3a7153915293a3728d3e7efaa95ad5a"),
        				new Artifact("target/argos-test-0.0.1-SNAPSHOT.jar","49e73a11c5e689db448d866ce08848ac5886cac8aa31156ea4de37427aca6162")))
        		.build();
        sig = CryptoHelper.sign(kp3, PASSWORD, jsonSigningSerializer.serialize(link));
        testStepLink = LinkMetaBlock.builder()
        		.link(link)
        		.signature(sig)
        		.supplyChainId(SUPPLY_CHAIN_ID)
        		.build();
        
    }

    @Test
    void verifyShouldProduceVerificationRunResult() {
        Artifact artifact = new Artifact("target/argos-test-0.0.1-SNAPSHOT.jar", "49e73a11c5e689db448d866ce08848ac5886cac8aa31156ea4de37427aca6162");
    	when(linkMetaBlockRepository.findBySupplyChainId(SUPPLY_CHAIN_ID)).thenReturn(List.of(buildStepLink, testStepLink));
        when(accountService.findPublicKeyByKeyId(kp1.getKeyId())).thenReturn(Optional.of(kp1));
        assertThat(verificationProvider.verifyRun(layoutMetaBlock, Set.of(artifact)).isRunIsValid(), is(true));
    }

    @Test
    void verifyWithWrongHashShouldProduceFalseVerificationRunResult() {
    	Artifact artifact = new Artifact("target/argos-test-0.0.1-SNAPSHOT.jar", "0123456789012345678901234567890012345678901234567890123456789012");
    	when(linkMetaBlockRepository.findBySupplyChainId(SUPPLY_CHAIN_ID)).thenReturn(List.of(buildStepLink, testStepLink));
    	VerificationRunResult res = verificationProvider.verifyRun(layoutMetaBlock, Set.of(artifact));
        assertThat(res.isRunIsValid(), is(false));
    }

}