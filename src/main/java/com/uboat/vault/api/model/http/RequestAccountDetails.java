package com.uboat.vault.api.model.http;

import com.uboat.vault.api.model.persistence.account.info.AccountDetails;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestAccountDetails {
    private String fullName;
    private String email;

    public RequestAccountDetails(AccountDetails accountDetails) {
        this.fullName = accountDetails.getFullName();
        this.email = accountDetails.getEmail();
    }
}
