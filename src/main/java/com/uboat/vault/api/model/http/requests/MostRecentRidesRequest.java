package com.uboat.vault.api.model.http.requests;

import com.uboat.vault.api.model.persistence.account.Account;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class MostRecentRidesRequest {
    private Account account;
    private int nrOfRides;
}
