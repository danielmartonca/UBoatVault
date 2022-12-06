package com.uboat.vault.api.model.domain.account.pending;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.uboat.vault.api.model.dto.AccountDTO;
import com.uboat.vault.api.model.enums.UserType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@NoArgsConstructor
@Entity
@Table(name = "PendingAccounts")
public class PendingAccount {
    @JsonIgnore
    @Id
    @Getter
    @Setter
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    private Long id;

    @Getter
    @Setter
    @Enumerated(EnumType.ORDINAL)
    private UserType type;

    @Getter
    @Setter
    private String username;

    @NotNull
    @Getter
    @Setter
    private String password;

    @NotNull
    @Getter
    @Setter
    private String email;

    @Getter
    @Setter
    private boolean isEmailVerified = false;


    @Getter
    @Setter
    private String token;

    /**
     * Creates a new account entity and a new pending token entity for the given account from the request
     *
     * @param account           account information from the request
     * @param registrationToken the RToken value of the new pending token
     */
    public PendingAccount(AccountDTO account, String registrationToken) {
        this.type = account.getType();
        this.username = account.getUsername();
        this.password = account.getPassword();
        this.email = account.getEmail();
        this.token = registrationToken;
    }
}