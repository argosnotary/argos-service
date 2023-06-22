package com.argosnotary.argos.domain.crypto;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.X509EncodedKeySpec;

import org.apache.commons.codec.binary.Hex;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PKCS8Generator;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcaPKCS8Generator;
import org.bouncycastle.openssl.jcajce.JceOpenSSLPKCS8DecryptorProviderBuilder;
import org.bouncycastle.openssl.jcajce.JceOpenSSLPKCS8EncryptorBuilder;
import org.bouncycastle.operator.InputDecryptorProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.OutputEncryptor;
import org.bouncycastle.pkcs.PKCS8EncryptedPrivateKeyInfo;
import org.bouncycastle.pkcs.PKCSException;
import org.bouncycastle.util.io.pem.PemGenerationException;
import org.bouncycastle.util.io.pem.PemObject;

import com.argosnotary.argos.domain.ArgosError;
import com.argosnotary.argos.domain.crypto.signing.SignatureAlgorithm;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class CryptoHelper {
	
	static {
		Security.addProvider(new BouncyCastleProvider());
	}

    public static Signature sign(KeyPair keyPair, char[] keyPassphrase, String jsonRepresentation) {
    	Signature sig = Signature.builder().keyId(keyPair.getKeyId()).build();
    	try {
			sig.setSignature(createSignature(decryptPrivateKey(keyPair, keyPassphrase), jsonRepresentation, sig.getAlgorithm()));
		} catch (GeneralSecurityException e) {
            throw new ArgosError(e.getMessage(), e);
		}
        return sig;
    }

    private static String createSignature(PrivateKey privateKey, String jsonRepr, SignatureAlgorithm algorithm) throws GeneralSecurityException {
        java.security.Signature privateSignature = java.security.Signature.getInstance(algorithm.getStringValue());
        privateSignature.initSign(privateKey);
        privateSignature.update(jsonRepr.getBytes(StandardCharsets.UTF_8));
        return Hex.encodeHexString(privateSignature.sign());
    }


    public static PrivateKey decryptPrivateKey(KeyPair keyPair, char[] keyPassphrase) {
        try {
            PKCS8EncryptedPrivateKeyInfo encPKInfo = new PKCS8EncryptedPrivateKeyInfo(keyPair.getEncryptedPrivateKey());
            InputDecryptorProvider decProv = new JceOpenSSLPKCS8DecryptorProviderBuilder().setProvider("BC").build(keyPassphrase);
            PrivateKeyInfo pkInfo = encPKInfo.decryptPrivateKeyInfo(decProv);
            return new JcaPEMKeyConverter().setProvider("BC").getPrivateKey(pkInfo);
        } catch (IOException | PKCSException | OperatorCreationException e) {
            throw new ArgosError(e.getMessage(), e);
        }
    }
    
	public static KeyPair createKeyPair(char[] passphrase) throws NoSuchAlgorithmException, OperatorCreationException, PemGenerationException {
		KeyPairGenerator generator = KeyPairGenerator.getInstance(KeyAlgorithm.EC.name());
		java.security.KeyPair keyPair = generator.generateKeyPair();
        JceOpenSSLPKCS8EncryptorBuilder encryptorBuilder = new JceOpenSSLPKCS8EncryptorBuilder(PKCS8Generator.AES_256_CBC).setProvider("BC");  
        OutputEncryptor encryptor = encryptorBuilder
        		.setRandom(new SecureRandom())
        		.setPassword(passphrase).build();
      
        JcaPKCS8Generator gen2 = new JcaPKCS8Generator(keyPair.getPrivate(), encryptor);  
        PemObject obj2 = gen2.generate();
        return new KeyPair(KeyIdProvider.computeKeyId(keyPair.getPublic()), 
        		keyPair.getPublic().getEncoded(), obj2.getContent());
	}

}
