package com.argosnotary.argos.service.account;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.argosnotary.argos.domain.account.Account;
import com.argosnotary.argos.domain.account.PersonalAccount;
import com.argosnotary.argos.domain.crypto.CryptoHelper;
import com.argosnotary.argos.domain.crypto.KeyPair;
import com.argosnotary.argos.service.account.AccountService;
import com.argosnotary.argos.service.account.AccountServiceImpl;
import com.argosnotary.argos.service.account.ClientRegistrationService;
import com.argosnotary.argos.service.account.ServiceAccountProviderService;
import com.argosnotary.argos.service.mongodb.account.PersonalAccountRepository;
import com.argosnotary.argos.service.mongodb.account.ServiceAccountRepository;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {
	
	private AccountService accountService; 
	
	private PersonalAccount pa1;
	private KeyPair kp1, kp2;

    @Mock
	private ServiceAccountRepository serviceAccountRepository;
    @Mock
    private PersonalAccountRepository personalAccountRepository;
    @Mock
    private ClientRegistrationService clientRegistrationService;
    @Mock
    private ServiceAccountProviderService serviceAccountProviderService;

	@BeforeEach
	void setUp() throws Exception {
		accountService = new AccountServiceImpl(serviceAccountRepository,personalAccountRepository,clientRegistrationService,serviceAccountProviderService);
		kp1 = CryptoHelper.createKeyPair("wachtwoord".toCharArray());
		kp2 = CryptoHelper.createKeyPair("wachtwoord".toCharArray());
		pa1 = PersonalAccount.builder().name("pa1").activeKeyPair(kp1).providerName("provider1").providerSubject("subject1").build();
	}

	@Test
	void testKeyPairExists() {
		when(personalAccountRepository.existsByActiveKey(kp1.getKeyId())).thenReturn(true);
		when(serviceAccountRepository.existsByActiveKey(kp1.getKeyId())).thenReturn(false);
		assertTrue(accountService.keyPairExists(kp1.getKeyId()));
		verify(personalAccountRepository).existsByActiveKey(kp1.getKeyId());
		verify(serviceAccountRepository).existsByActiveKey(kp1.getKeyId());
	}

	@Test
	void testFindKeyPairByKeyId() {
		when(personalAccountRepository.findFirstByActiveKeyId(kp1.getKeyId())).thenReturn(Optional.of(pa1));
		when(serviceAccountRepository.findFirstByActiveKeyId(kp1.getKeyId())).thenReturn(Optional.empty());
		assertThat(accountService.findKeyPairByKeyId(kp1.getKeyId()), is(Optional.of(kp1)));
		verify(personalAccountRepository).findFirstByActiveKeyId(kp1.getKeyId());
		verify(serviceAccountRepository).findFirstByActiveKeyId(kp1.getKeyId());
		
	}

	@Test
	void testLoadAuthenticatedUser() {
		Optional<String> optProviderName = Optional.of("optProviderName");
		when(clientRegistrationService.getClientRegistrationName("provider1")).thenReturn(optProviderName);
		when(serviceAccountProviderService.isProviderIssuer("provider1")).thenReturn(false);
		when(personalAccountRepository.findFirstByProviderNameAndProviderSubject(optProviderName.get(), "subject1")).thenReturn(Optional.of(pa1));
		
		Optional<Account> acc = accountService.loadAuthenticatedUser("provider1","subject1");
		assertEquals(pa1, acc.get());
		
	}

}
