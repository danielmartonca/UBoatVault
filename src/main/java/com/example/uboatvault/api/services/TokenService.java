package com.example.uboatvault.api.services;

import com.example.uboatvault.api.repositories.RegistrationDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TokenService {

    RegistrationDataRepository registrationDataRepository;
    EncryptionService encryptionService;

    @Autowired
    public TokenService(RegistrationDataRepository registrationDataRepository, EncryptionService encryptionService) {
        this.registrationDataRepository = registrationDataRepository;
        this.encryptionService = encryptionService;
    }

    public boolean isTokenValid(String token) {
        var registrationData = registrationDataRepository.findFirstByToken(token);
        return registrationData != null;
    }

    public boolean isTokenDecryptable(String token) {
        String decryptedToken = encryptionService.decryptString(token);
        return !decryptedToken.isEmpty();
    }
}
