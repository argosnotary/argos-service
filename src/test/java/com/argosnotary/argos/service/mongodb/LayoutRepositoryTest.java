package com.argosnotary.argos.service.mongodb;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.junit.jupiter.EnabledIf;

@EnabledIf(expression = "#{environment['spring.profiles.active'] == 'itest'}")
class LayoutRepositoryTest {

	@BeforeEach
	void setUp() throws Exception {
	}

	@Test
	void test() {
		//fail("Not yet implemented");
	}

}
