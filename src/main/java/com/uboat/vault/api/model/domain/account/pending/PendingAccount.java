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

    @Getter
    @NotNull
    @Setter
    private String password;

    @JsonIgnore
    @Getter
    @Setter
    @OneToOne(cascade = {CascadeType.ALL})
    @JoinColumn(name = "pending_token_id")
    private PendingToken pendingToken;

    public PendingAccount(AccountDTO account) {
        this.type = account.getType();
        this.username = account.getUsername();
        this.password = account.getPassword();
    }
}