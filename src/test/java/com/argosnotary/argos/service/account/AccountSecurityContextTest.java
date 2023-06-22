package com.argosnotary.argos.service.account;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.argosnotary.argos.domain.account.Account;
import com.argosnotary.argos.domain.account.PersonalAccount;
import com.argosnotary.argos.service.account.AccountSecurityContext;
import com.argosnotary.argos.service.account.AccountSecurityContextImpl;
import com.argosnotary.argos.service.account.ArgosUserDetails;

@ExtendWith(MockitoExtension.class)
class AccountSecurityContextTest {
	
	private AccountSecurityContext accountSecurityContext;
	
	@Mock
	private SecurityContext securityContext;
	@Mock
	private ArgosUserDetails argosUserDetails;
	@Mock
	private Authentication authentication;
	
	private PersonalAccount pa;

	@BeforeEach
	void setUp() throws Exception {
		
		accountSecurityContext = new AccountSecurityContextImpl();
		SecurityContextHolder.setContext(securityContext);
		pa = PersonalAccount.builder().name("pa").build();
		argosUserDetails = new ArgosUserDetails(pa);
		
	}

	@Test
	void testGetAuthenticatedAccount() {
		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(authentication.getPrincipal()).thenReturn(argosUserDetails);
		assertEquals(Optional.of(pa), accountSecurityContext.getAuthenticatedAccount());
		
		
	}

}
