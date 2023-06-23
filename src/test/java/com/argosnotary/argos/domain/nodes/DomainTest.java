package com.argosnotary.argos.domain.nodes;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DomainTest {

	@BeforeEach
	void setUp() throws Exception {
	}

	@Test
	void testReverseDomain() {
		Domain domain = Domain.builder().domain("org1.com").build();
		List<String> labels = domain.reverseLabels();
		assertThat(labels, is(List.of("com", "org1")));
		assertThat(labels.size(), is(2));
	}

}
