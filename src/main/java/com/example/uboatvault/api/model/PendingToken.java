package com.example.uboatvault.api.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Entity
@Table(name = "PendingTokens")
public class PendingToken {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    @Setter
    @Column(nullable = false, unique = true)
    String tokenValue;
}
