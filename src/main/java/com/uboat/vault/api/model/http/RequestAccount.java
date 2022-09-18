package com.uboat.vault.api.model.http;

import com.uboat.vault.api.model.enums.UserType;
import com.uboat.vault.api.model.persistence.account.pending.PendingAccount;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestAccount {
    @NotNull
    private UserType type;

    private String username;
    @NotNull
    private String password;

    private RequestPhoneNumber phoneNumber;

    private RequestRegistrationData registrationData;

    public boolean equalsPendingAccount(PendingAccount pendingAccount) {
        return this.username.equals(pendingAccount.getUsername()) &&
                this.password.equals(pendingAccount.getPassword());
    }
}
