package com.argosnotary.argos.domain.crypto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class HashAlgorithmTest {

	@Test
	void testGetStringValue() {
		HashAlgorithm alg = HashAlgorithm.SHA256;
		assertEquals("SHA-256", alg.getStringValue());
	}

}
