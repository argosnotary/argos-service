/*
 * Argos Notary - A new way to secure the Software Supply Chain
 *
 * Copyright (C) 2019 - 2020 Rabobank Nederland
 * Copyright (C) 2019 - 2025 Gerard Borst <gerard.borst@argosnotary.com>
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
package com.argosnotary.argos.service.itest.crypto;


import java.util.List;

import org.mapstruct.Mapper;

import com.argosnotary.argos.service.itest.rest.api.model.RestArtifact;
import com.argosnotary.argos.service.itest.rest.api.model.RestLayout;
import com.argosnotary.argos.service.itest.rest.api.model.RestLink;
import com.argosnotary.argos.service.itest.rest.api.model.RestMatchRule;
import com.argosnotary.argos.service.itest.rest.api.model.RestRule;
import com.argosnotary.argos.service.itest.rest.api.model.RestStep;

/**
 * By defining all methods, we force MapStruct to generate new objects for each mapper in stead of
 * taking shortcuts by mapping an object directly.
 */
@Mapper
public interface Cloner {

    RestLink clone(RestLink link);

    List<RestArtifact> cloneArtifacts(List<RestArtifact> artifacts);

    RestArtifact clone(RestArtifact artifact);
    
    RestLayout clone(RestLayout layout);

    List<RestStep> cloneSteps(List<RestStep> steps);

    RestStep clone(RestStep step);

    List<RestRule> clone(List<RestRule> rules);
    
    RestRule clone(RestRule rule);
    
//    default RestRule clone(RestRule rule) {
//        if (rule.getRuleType().equals(RestRule.RuleTypeEnum.MATCH)) {
//            return clone((RestMatchRule) rule);
//        } else {
//            return new RestRule(rule.getRuleType(), rule.getPattern());
//        }
//
//    }
    
    RestMatchRule clone(RestMatchRule matchRule);

}
