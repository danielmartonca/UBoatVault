package com.example.uboatvault.api.model.response;

import com.example.uboatvault.api.services.EncryptionService;
import lombok.Data;

@Data
public class RegistrationDataResponse {
    private final Boolean isDeviceRegistered;
    private final String token;

    public RegistrationDataResponse(Boolean isDeviceRegistered) {
        this.isDeviceRegistered = isDeviceRegistered;
        this.token = null;
    }

    public RegistrationDataResponse(Boolean isDeviceRegistered, String token) {
        this.isDeviceRegistered = isDeviceRegistered;
        if (token == null) this.token = null;
        else if (EncryptionService.isTokenDecryptable(token))
            this.token = EncryptionService.encryptString(token);
        else
            this.token = token;

    }
}
