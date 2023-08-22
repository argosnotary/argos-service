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
package com.argosnotary.argos.service.rest.layout;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.argosnotary.argos.service.openapi.rest.model.RestHashAlgorithm;
import com.argosnotary.argos.service.openapi.rest.model.RestKeyAlgorithm;
import com.argosnotary.argos.service.openapi.rest.model.RestLayout;
import com.argosnotary.argos.service.openapi.rest.model.RestLayoutMetaBlock;
import com.argosnotary.argos.service.openapi.rest.model.RestMatchRule;
import com.argosnotary.argos.service.openapi.rest.model.RestMatchRule.RuleTypeEnum;
import com.argosnotary.argos.service.openapi.rest.model.RestPublicKey;
import com.argosnotary.argos.service.openapi.rest.model.RestRule;
import com.argosnotary.argos.service.openapi.rest.model.RestSignature;
import com.argosnotary.argos.service.openapi.rest.model.RestStep;
import com.argosnotary.argos.service.rest.ValidateHelper;
import com.argosnotary.argos.service.rest.ValidateHelper.ValidationError;

class RestLayoutMetaBlockTest {


    @Test
    void emptyRestLayoutMetaBlock() {
        List<ValidationError> errors = ValidateHelper.validate(new RestLayoutMetaBlock());
        assertThat(ValidateHelper.validate(new RestLayoutMetaBlock()), contains(ValidateHelper.expectedErrors(
                "layout", "must not be null",
                "signatures", "size must be between 1 and 20")));
    }

    @Test
    void emptyRestLayout() {
        assertThat(ValidateHelper.validate(new RestLayoutMetaBlock()
                .addSignaturesItem(new RestSignature())
                .layout(new RestLayout())), contains(ValidateHelper.expectedErrors(
                "layout.authorizedKeyIds", "size must be between 1 and 256",
                "layout.expectedEndProducts", "size must be between 1 and 4096",
                "layout.keys", "size must be between 1 and 256",
                "layout.steps", "size must be between 1 and 256",
                "signatures[0].hashAlgorithm", "must not be null",
                "signatures[0].keyAlgorithm", "must not be null",
                "signatures[0].keyId", "must not be null",
                "signatures[0].sig", "must not be null")));
    }

    @Test
    void emptySubItemsRestLayout() {

        assertThat(ValidateHelper.validate(new RestLayoutMetaBlock()
                .addSignaturesItem(new RestSignature()
                        .keyId("keyId")
                        .sig("signature")
                        .hashAlgorithm(RestHashAlgorithm.SHA256)
                        .keyAlgorithm(RestKeyAlgorithm.EC))
                .layout(new RestLayout()
                        .addAuthorizedKeyIdsItem("authorizedKeyId")
                        .addExpectedEndProductsItem(new RestMatchRule())
                        .addKeysItem(new RestPublicKey()))), contains(ValidateHelper.expectedErrors(
                "layout.expectedEndProducts[0].destinationStepName", "must not be null",
                "layout.expectedEndProducts[0].destinationType", "must not be null",
                "layout.expectedEndProducts[0].pattern", "must not be null",
                "layout.expectedEndProducts[0].ruleType", "must not be null",
                "layout.keys[0].keyId", "must not be null",
                "layout.keys[0].pub", "must not be null",
                "layout.steps", "size must be between 1 and 256",
                "signatures[0].keyId", "must match \"^[0-9a-f]*$\"",
                "signatures[0].keyId", "size must be between 24 and 128",
                "signatures[0].sig", "must match \"^[0-9a-f]*$\""
                )));
    }

    @Test
    void subItemsRestLayout() {
        assertThat(ValidateHelper.validate(new RestLayoutMetaBlock()
                .addSignaturesItem(createSignature())
                .layout(new RestLayout()
                        .addAuthorizedKeyIdsItem("authorizedKeyId")
                        .addExpectedEndProductsItem(new RestMatchRule()
                                .destinationStepName("step 1")
                                .destinationType(RestMatchRule.DestinationTypeEnum.PRODUCTS)
                                .ruleType(RuleTypeEnum.MATCH)
                                .pattern("pattern"))
                        .addKeysItem(new RestPublicKey()
                                .keyId("keyId")
                                .pub(new byte[]{1}))
                        .addStepsItem(new RestStep())
                        )
                ), contains(ValidateHelper.expectedErrors(
                                "layout.expectedEndProducts[0].destinationStepName", "must match \"^([a-z]|[a-z][a-z0-9-]*[a-z0-9])?$\"",
                                "layout.keys[0].keyId", "must match \"^[0-9a-f]*$\"",
                                "layout.keys[0].keyId", "size must be between 24 and 128",
                                "layout.steps[0].authorizedKeyIds", "size must be between 1 and 256",
                                "layout.steps[0].name", "must not be null",
                                "layout.steps[0].requiredNumberOfLinks", "must not be null")));
    }

    @Test
    void stepItemsRestLayout() {        
        assertThat(ValidateHelper.validate(new RestLayoutMetaBlock()
                .addSignaturesItem(createSignature())
                .layout(new RestLayout()
                        .addAuthorizedKeyIdsItem("authorizedKeyId")
                        .addExpectedEndProductsItem(new RestMatchRule()
                                .destinationStepName("step1")
                                .destinationType(RestMatchRule.DestinationTypeEnum.PRODUCTS)
                                .ruleType(RuleTypeEnum.MATCH)
                                .pattern("pattern"))
                        .addKeysItem(new RestPublicKey()
                                .keyId("c8df0a497ab0df7136c4f97892f17914e6e5e021fdc039f0ea7c27d5a95c1254")
                                .pub(new byte[]{1}))
                        .addStepsItem(new RestStep()
                                .addExpectedMaterialsItem(new RestRule())
                                .addExpectedProductsItem(new RestRule())
                                .name("step 1")
                                .addAuthorizedKeyIdsItem("authorizedKeyId"))
                        )
                ), contains(ValidateHelper.expectedErrors(
                "layout.steps[0].expectedMaterials[0].pattern", "must not be null",
                "layout.steps[0].expectedMaterials[0].ruleType", "must not be null",
                "layout.steps[0].expectedProducts[0].pattern", "must not be null",
                "layout.steps[0].expectedProducts[0].ruleType", "must not be null",
                "layout.steps[0].name", "must match \"^([a-z]|[a-z][a-z0-9-]*[a-z0-9])?$\"",
                "layout.steps[0].requiredNumberOfLinks", "must not be null"
        )));
    }

    @Test
    void stepItemsRestLayoutRules() {
        assertThat(ValidateHelper.validate(new RestLayoutMetaBlock()
                .addSignaturesItem(createSignature())
                .layout(new RestLayout()
                        .addAuthorizedKeyIdsItem("authorizedKeyId")
                        .addExpectedEndProductsItem(new RestMatchRule()
                                .destinationStepName("step1")
                                .destinationType(RestMatchRule.DestinationTypeEnum.PRODUCTS)
                                .ruleType(RuleTypeEnum.MATCH)
                                .pattern("pattern"))
                        .addKeysItem(new RestPublicKey()
                                .keyId("c8df0a497ab0df7136c4f97892f17914e6e5e021fdc039f0ea7c27d5a95c1254")
                                .pub(new byte[]{1}))
                        .addStepsItem(new RestStep()
                                .requiredNumberOfLinks(1)
                                .addExpectedMaterialsItem(new RestRule().ruleType(RestRule.RuleTypeEnum.MATCH).pattern("pattern"))
                                .addExpectedProductsItem(new RestRule().ruleType(RestRule.RuleTypeEnum.CREATE).pattern("pattern"))
                                .name("step1")
                                .addAuthorizedKeyIdsItem("authorizedKeyId"))
                        )
                ), empty()
        );
    }

    private RestSignature createSignature() {
        return new RestSignature()
                .hashAlgorithm(RestHashAlgorithm.SHA256)
                .keyAlgorithm(RestKeyAlgorithm.EC)
                .keyId("c8df0a497ab0df7136c4f97892f17914e6e5e021fdc039f0ea7c27d5a95c1254")
                .sig("c8df0a497ab0df7136c4f97892f17914e6e5e021fdc039f0ea7c27d5a95c1254c8df0a497ab0df7136c4f97892f17914e6e5e021fdc039f0ea7c27d5a95c1254c8df0a497ab0df7136c4f97892f17914e6e5e021fdc039f0ea7c27d5a95c1254c8df0a497ab0df7136c4f97892f17914e6e5e021fdc039f0ea7c27d5a95c1254c8df0a497ab0df7136c4f97892f17914e6e5e021fdc039f0ea7c27d5a95c1254c8df0a497ab0df7136c4f97892f17914e6e5e021fdc039f0ea7c27d5a95c1254c8df0a497ab0df7136c4f97892f17914e6e5e021fdc039f0ea7c27d5a95c1254c8df0a497ab0df7136c4f97892f17914e6e5e021fdc039f0ea7c27d5a95c1254c8df0a497ab0df7136c4f97892f17914e6e5e021fdc039f0ea7c27d5a95c1254c8df0a497ab0df7136c4f97892f17914e6e5e021fdc039f0ea7c27d5a95c1254c8df0a497ab0df7136c4f97892f17914e6e5e021fdc039f0ea7c27d5a95c1254c8df0a497ab0df7136c4f97892f17914e6e5e021fdc039f0ea7c27d5a95c1254");
    }
}