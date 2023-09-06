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
package com.argosnotary.argos.domain.attest;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.argosnotary.argos.domain.crypto.HashAlgorithm;

class ArgosDigestTest {

	@BeforeEach
	void setUp() throws Exception {
	}

	@Test
	void testCompareTo() {
		ArgosDigest ad1 = new ArgosDigest("hash1", HashAlgorithm.SHA256);
		ArgosDigest ad12 = new ArgosDigest("hash1", HashAlgorithm.SHA256);
		ArgosDigest ad2 = new ArgosDigest("hash2", HashAlgorithm.SHA256);
		assertEquals(0, ad1.compareTo(ad12));
		assertEquals(-1, ad1.compareTo(ad2));
		assertEquals(1, ad2.compareTo(ad1));
		
	}

}
