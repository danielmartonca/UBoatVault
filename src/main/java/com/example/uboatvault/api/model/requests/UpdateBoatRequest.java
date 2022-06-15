package com.example.uboatvault.api.model.requests;

import com.example.uboatvault.api.model.persistence.account.Account;
import com.example.uboatvault.api.model.persistence.sailing.sailor.Boat;
import lombok.Getter;
import lombok.Setter;

public class UpdateBoatRequest {
    @Getter
    @Setter
    private Account account;
    @Getter
    @Setter
    private Boat boat;
}
