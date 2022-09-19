package com.uboat.vault.api.model.http;

import com.uboat.vault.api.model.enums.UserType;
import com.uboat.vault.api.model.persistence.account.Account;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestAccountDetails {
    private UserType type;
    private String fullName;
    private String email;

    public RequestAccountDetails(Account account) {
        this.type = account.getType();

        this.fullName = account.getAccountDetails().getFullName();
        if (this.fullName == null)
            this.fullName = account.getUsername();

        this.email = account.getAccountDetails().getEmail();
    }
}
