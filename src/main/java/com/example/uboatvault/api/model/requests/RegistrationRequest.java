package com.example.uboatvault.api.model.requests;

import com.example.uboatvault.api.model.persistence.account.RegistrationData;
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
    @Setter
    @JsonIgnore
    private String token;
    private String phoneNumber;
    private String username;
    private String password;
    @Setter
    private RegistrationData registrationData;


    @Override
    public String toString() {
        return "RegistrationRequest{\n" +
                "token:'" + token + '\'' + '\n' +
                ", phoneNumber:'" + phoneNumber + '\'' + '\n' +
                ", username:'" + username + '\'' + '\n' +
                ", password:'" + "***" + '\'' + '\n' +
                ", registrationData:" + registrationData + '\n' +
                '}';
    }
}
