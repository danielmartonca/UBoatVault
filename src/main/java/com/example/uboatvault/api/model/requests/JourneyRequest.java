package com.example.uboatvault.api.model.requests;

import com.example.uboatvault.api.model.persistence.account.Account;
import com.example.uboatvault.api.model.persistence.location.LocationData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class JourneyRequest {
    private Account account;
    private String token;
    private LocationData locationData;
}
