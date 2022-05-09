package com.example.uboatvault.api.model.requests;

import com.example.uboatvault.api.model.persistence.RegistrationData;
import com.example.uboatvault.api.services.EncryptionService;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.Transient;

@AllArgsConstructor
@Getter
public class RegistrationRequest {
    @Autowired
    @Transient
    @JsonIgnore
    EncryptionService encryptionService;

    @Setter
    @JsonIgnore
    private String token;
    private String phoneNumber;
    private String username;
    private String password;
    @Setter
    private RegistrationData registrationData;

    private void decryptCredentials() {
        this.username = encryptionService.decryptString(this.username);
        this.password = encryptionService.decryptString(this.password);
    }

    @Override
    public String toString() {
        return "RegistrationRequest{" +
                "token='" + token + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", username='" + "***" + '\'' +
                ", password='" + "***" + '\'' +
                ", registrationData=" + registrationData +
                '}';
    }
}
