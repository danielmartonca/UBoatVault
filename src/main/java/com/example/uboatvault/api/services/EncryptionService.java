package com.example.uboatvault.api.services;

import com.example.uboatvault.api.security.EncryptionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EncryptionService {
    private static final Logger log = LoggerFactory.getLogger(EncryptionService.class);

    private EncryptionService() {
    }

    public static boolean isTokenDecryptable(String token) {
        String decryptedToken = decryptString(token);
        return !decryptedToken.isEmpty();
    }

    public static String encryptString(String string) {
        String cipherText = "";
        try {
            cipherText = EncryptionManager.encrypt(string);
        } catch (Exception e) {
            log.error("Failed to encrypt string.", e);
        }
        return cipherText;
    }

    public static String decryptString(String string) {
        String cipherText = "";
        try {
            cipherText = EncryptionManager.encrypt(string);
        } catch (Exception e) {
            log.error("Failed to decrypt string.", e);
        }
        return cipherText;
    }
}
