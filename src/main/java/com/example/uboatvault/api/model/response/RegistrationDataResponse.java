package com.example.uboatvault.api.model.response;

import com.example.uboatvault.api.services.EncryptionService;
import com.example.uboatvault.api.services.TokenService;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;

@Data
public class RegistrationDataResponse {
    @Autowired
    @JsonIgnore
    EncryptionService encryptionService;
    @Autowired
    @JsonIgnore
    TokenService tokenService;


    private final Boolean isDeviceRegistered;
    private final String token;

    public RegistrationDataResponse(Boolean isDeviceRegistered) {
        this.isDeviceRegistered = isDeviceRegistered;
        this.token = null;
    }

    public RegistrationDataResponse(Boolean isDeviceRegistered, String token) {
        this.isDeviceRegistered = isDeviceRegistered;
        if (token == null) this.token = null;
        else if (tokenService.isTokenDecryptable(token))
            this.token = encryptionService.encryptString(token);
        else
            this.token = token;

    }
}
