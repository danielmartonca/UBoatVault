package com.uboat.vault.api.services;

import com.uboat.vault.api.security.EncryptionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class EncryptionService {
    private static final Logger log = LoggerFactory.getLogger(EncryptionService.class);

    public String encryptString(String string) {
        String cipherText = "";
        try {
            cipherText = EncryptionManager.encrypt(string);
        } catch (Exception e) {
            log.error("Failed to encrypt string.", e);
        }
        return cipherText;
    }

    public String decryptString(String string) {
        String cipherText = "";
        try {
            cipherText = EncryptionManager.encrypt(string);
        } catch (Exception e) {
            log.error("Failed to decrypt string.", e);
        }
        return cipherText;
    }
}
