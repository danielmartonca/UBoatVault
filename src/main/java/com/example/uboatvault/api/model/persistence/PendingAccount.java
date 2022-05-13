package com.example.uboatvault.api.model.persistence;

import com.example.uboatvault.api.model.enums.UserType;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
    private UserType userType;

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

    public PendingAccount(Account account) {
        this.userType = account.getUserType();
        this.username = account.getUsername();
        this.password = account.getPassword();
    }
}