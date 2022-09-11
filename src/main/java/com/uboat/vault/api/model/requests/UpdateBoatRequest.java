package com.uboat.vault.api.model.requests;

import com.uboat.vault.api.model.persistence.account.Account;
import com.uboat.vault.api.model.persistence.sailing.sailor.Boat;
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
