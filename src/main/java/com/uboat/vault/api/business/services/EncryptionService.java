package com.uboat.vault.api.business.services;

import com.uboat.vault.api.security.EncryptionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class EncryptionService {

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
