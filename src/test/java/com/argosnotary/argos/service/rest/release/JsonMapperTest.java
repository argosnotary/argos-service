package com.argosnotary.argos.service.rest.release;

import static org.junit.jupiter.api.Assertions.fail;

import java.time.OffsetDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.argosnotary.argos.service.JsonMapperConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest(classes={JsonMapperConfig.class})
class JsonMapperTest {
	
	@Autowired
	ObjectMapper objectMapper;

	@BeforeEach
	void setUp() throws Exception {
	}

	@Test
	void test() throws JsonProcessingException {
		OffsetDateTime time = OffsetDateTime.now();
		
		String timeStr = objectMapper.writeValueAsString(time);
		
	}

}
