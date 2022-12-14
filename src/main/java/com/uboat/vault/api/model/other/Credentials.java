package com.uboat.vault.api.model.other;

import com.uboat.vault.api.model.domain.account.account.Account;
import com.uboat.vault.api.model.dto.AccountDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Credentials {
    private final String phoneNumber;
    private final String username;
    private final String password;

    private Credentials(AccountDTO account) {
        if (account.getPhoneNumber() != null)
            this.phoneNumber = account.getPhoneNumber().getNumber();
        else
            this.phoneNumber = "";

        if (account.getUsername() != null)
            this.username = account.getUsername();
        else
            this.username = "";

        this.password = account.getPassword();
    }

    private Credentials(Account account) {
        this.phoneNumber = account.getPhone().getNumber();
        this.username = account.getUsername();
        this.password = account.getPassword();
    }

    public static Credentials fromAccount(Account account) {
        return new Credentials(account);
    }

    public static Credentials fromRequest(AccountDTO account) {
        return new Credentials(account);
    }
}
