package com.argosnotary.argos.domain.crypto.signing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.argosnotary.argos.domain.crypto.HashAlgorithm;
import com.argosnotary.argos.domain.crypto.KeyAlgorithm;

class SignatureAlgorithmTest {

	@BeforeEach
	void setUp() throws Exception {
	}

	@Test
	void testGetAlgorithm() {
		Throwable exception = assertThrows(java.security.GeneralSecurityException.class, () -> {
			SignatureAlgorithm.getAlgorithm(KeyAlgorithm.EC, HashAlgorithm.SHA256);
          });
        
        assertEquals("Combination of algorithms [EC] and [SHA256] not supported", exception.getMessage());
	}

}
