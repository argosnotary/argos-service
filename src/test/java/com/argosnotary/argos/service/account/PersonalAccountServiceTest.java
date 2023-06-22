package com.argosnotary.argos.service.account;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.argosnotary.argos.domain.account.PersonalAccount;
import com.argosnotary.argos.domain.crypto.CryptoHelper;
import com.argosnotary.argos.domain.crypto.KeyPair;
import com.argosnotary.argos.service.account.PersonalAccountService;
import com.argosnotary.argos.service.account.PersonalAccountServiceImpl;
import com.argosnotary.argos.service.mongodb.account.PersonalAccountRepository;

@ExtendWith(MockitoExtension.class)
class PersonalAccountServiceTest {
	
	private PersonalAccountService personalAccountService; 
	
	private PersonalAccount pa1;
	private KeyPair kp1, kp2;
    @Mock
    private PersonalAccountRepository personalAccountRepository;

	@BeforeEach
	void setUp() throws Exception {
		personalAccountService = new PersonalAccountServiceImpl(personalAccountRepository);
		kp1 = CryptoHelper.createKeyPair("wachtwoord".toCharArray());
		kp2 = CryptoHelper.createKeyPair("wachtwoord".toCharArray());
		pa1 = PersonalAccount.builder().name("pa1").activeKeyPair(kp1).providerName("provider1").providerSubject("subject1").build();
	}

	@Test
	void testActivateNewKey() {
		personalAccountService.activateNewKey(pa1, kp2);
		pa1.deactivateKeyPair(kp2);
		verify(personalAccountRepository).save(pa1);
		
	}

}
