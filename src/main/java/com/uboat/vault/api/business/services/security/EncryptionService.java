package com.uboat.vault.api.business.services.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

@Service
@Slf4j
@RequiredArgsConstructor
public class EncryptionService {
    @Value("${uboat.security.encryption.password}")
    private String encryptionKey;
    @Value("${uboat.security.encryption.algorithm}")
    private String encryptionAlgorithm;

    private IvParameterSpec ivParameterSpec;
    private SecretKey secretKey;

    @PostConstruct
    public void init() throws NoSuchAlgorithmException, InvalidKeySpecException {
        ivParameterSpec = generateIv();
        secretKey = getSecretKeyFromPassword(encryptionKey, RandomStringUtils.randomAlphabetic(10));
    }

    private IvParameterSpec generateIv() {
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        return new IvParameterSpec(iv);
    }

    private SecretKey getSecretKeyFromPassword(String password, String salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
        var factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        var spec = new PBEKeySpec(password.toCharArray(), salt.getBytes(), 65536, 256);
        return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
    }

    public String encrypt(String plaintext) {
        try {
            var cipher = Cipher.getInstance(encryptionAlgorithm);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec);
            var cipherText = cipher.doFinal(plaintext.getBytes());
            return Base64.getEncoder().encodeToString(cipherText);
        } catch (Exception e) {
            log.error("Exception occurred while trying to encrypt the plaintext.", e);
            throw new RuntimeException(e);
        }
    }

    public String decrypt(String plaintext) {
        try {
            var cipher = Cipher.getInstance(encryptionAlgorithm);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec);
            var cipherText = cipher.doFinal(plaintext.getBytes());
            return Base64.getEncoder().encodeToString(cipherText);
        } catch (Exception e) {
            log.error("Exception occurred while trying to decrypt the ciphertext.", e);
            throw new RuntimeException(e);
        }
    }
}
