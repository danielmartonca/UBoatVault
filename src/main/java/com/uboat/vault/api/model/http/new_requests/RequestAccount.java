package com.uboat.vault.api.model.http.new_requests;

import com.uboat.vault.api.model.enums.UserType;
import com.uboat.vault.api.model.persistence.account.info.PhoneNumber;
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
    private UserType type;
    @NotNull
    private String username;
    @NotNull
    private String password;
    @NotNull
    private PhoneNumber phoneNumber;

    @NotNull
    private RequestRegistrationData registrationData;

    public boolean equalsPendingAccount(PendingAccount pendingAccount) {
        return this.username.equals(pendingAccount.getUsername()) &&
                this.password.equals(pendingAccount.getPassword());
    }
}
