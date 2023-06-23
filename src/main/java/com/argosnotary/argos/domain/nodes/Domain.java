package com.argosnotary.argos.domain.nodes;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class Domain {
	String domain;

	public List<String> reverseLabels() {
		List<String> reverse = Arrays.asList(domain.split("\\."));
		Collections.reverse(reverse);
		return reverse;
	}

}
