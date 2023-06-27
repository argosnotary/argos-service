package com.argosnotary.argos.service.security.oauth;

import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

import com.argosnotary.argos.service.security.helpers.OAuth2AuthorizationRequestDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;

@Component
public class OAuth2AuthorizationRequestModule extends SimpleModule {
	
	public OAuth2AuthorizationRequestModule() {
		super();
		this.addDeserializer(OAuth2AuthorizationRequest.class, new OAuth2AuthorizationRequestDeserializer());
	}
	

}
