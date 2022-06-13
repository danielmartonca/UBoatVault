package com.example.uboatvault.api.model.requests;

import com.example.uboatvault.api.model.persistence.account.Account;
import com.example.uboatvault.api.model.persistence.sailing.LocationData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class PulseRequest {
    private Account account;
    private LocationData locationData;
    boolean lookingForClients;
}
