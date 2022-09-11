package com.uboat.vault.api.model.requests;

import com.uboat.vault.api.model.persistence.account.Account;
import com.uboat.vault.api.model.persistence.sailing.Journey;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class SelectClientRequest {
    private Account account;
    private Journey journey;
}
