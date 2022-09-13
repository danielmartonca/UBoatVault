package com.uboat.vault.api.model.other;

import com.uboat.vault.api.model.http.new_requests.RequestAccount;
import com.uboat.vault.api.model.persistence.account.Account;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Credentials {
    private final String phoneNumber;
    private final String username;
    private final String password;

    private Credentials(RequestAccount account) {
        if (account.getPhoneNumber() != null)
            this.phoneNumber = account.getPhoneNumber().getPhoneNumber();
        else
            this.phoneNumber = "";

        if (account.getUsername() != null)
            this.username = account.getUsername();
        else
            this.username = "";

        this.password = account.getPassword();
    }

    private Credentials(Account account) {
        if (account.getPhoneNumber() != null)
            this.phoneNumber = account.getPhoneNumber().getPhoneNumber();
        else
            this.phoneNumber = "";

        if (account.getUsername() != null)
            this.username = account.getUsername();
        else
            this.username = "";

        this.password = account.getPassword();
    }

    public static Credentials fromAccount(Account account) {
        return new Credentials(account);
    }

    public static Credentials fromRequest(RequestAccount account) {
        return new Credentials(account);
    }
}
