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
package com.argosnotary.argos.service.rest.oauthprovider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import com.argosnotary.argos.service.account.OAuthProvider;
import com.argosnotary.argos.service.openapi.rest.model.RestOAuthProvider;

class OauthProviderMapperTest {

	@BeforeEach
	void setUp() throws Exception {
	}

	@Test
	void test() {
		OauthProviderMapper mapper = Mappers.getMapper(OauthProviderMapper.class);
		OAuthProvider p = new OAuthProvider("oauth-provider");
		RestOAuthProvider rp = mapper.convertToRestOauthProvider(p);
		assertEquals(p.getName(), rp.getName());
		
		List<RestOAuthProvider> lrp = mapper.convertToRestOAuthProviderList(List.of(p));
		assertEquals(lrp.get(0), rp);
		
		lrp = mapper.convertToRestOAuthProviderList(List.of());
		assertTrue(lrp.isEmpty());
		
	}

}
