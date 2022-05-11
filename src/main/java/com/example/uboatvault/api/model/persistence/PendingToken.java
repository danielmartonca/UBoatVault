package com.example.uboatvault.api.model.persistence;

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
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Setter
    @Column(nullable = false, unique = true)
    private String tokenValue;

    @JsonIgnore
    @OneToOne(cascade = CascadeType.MERGE)
    private PendingAccount account;

    public PendingToken(String tokenValue,PendingAccount account) {
        this.tokenValue = tokenValue;
        this.account=account;
    }
}
