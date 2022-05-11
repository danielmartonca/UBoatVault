package com.example.uboatvault.api.model.persistence;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@NoArgsConstructor
@Entity
@Table(name = "PendingAccounts")
public class PendingAccount {
    @Id
    @Getter
    @Setter
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    @Getter
    @Setter
    private String username;

    @Getter
    @NotNull
    @Setter
    private String password;

    public PendingAccount(Account account) {
        this.username = account.getUsername();
        this.password = account.getPassword();
    }
}