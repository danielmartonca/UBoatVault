package com.uboat.vault.api.model.requests;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.uboat.vault.api.model.persistence.account.info.RegistrationData;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

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
