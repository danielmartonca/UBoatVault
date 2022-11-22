package com.uboat.vault.api.model.domain.account.pending;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Entity
@Table(name = "PendingTokens")
public class PendingToken {
    @JsonIgnore
    @Id
    @Getter
    @Setter
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    private Long id;

    @Setter
    @Column(nullable = false, unique = true)
    private String tokenValue;

    @JsonIgnore
    @Getter
    @Setter
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "pending_account_id")
    private PendingAccount account;

    public PendingToken(String tokenValue, PendingAccount pendingAccount) {
        this.tokenValue = tokenValue;
        this.account = pendingAccount;
        this.account.setPendingToken(this);
    }
}
