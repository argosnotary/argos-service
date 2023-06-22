package com.argosnotary.argos.service.rest.account;

import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import com.argosnotary.argos.domain.account.ServiceAccount;

class ServiceAccountMapperTest {
	
	private ServiceAccountMapper serviceAccountMapper;

	@BeforeEach
	void setUp() throws Exception {
		serviceAccountMapper = Mappers.getMapper(ServiceAccountMapper.class);
	}

	@Test
	void testConvertToRestServiceAccount() {
		
	}
    
	@Test
	void testConvertFromRestServiceAccount() {
		
	}

}
