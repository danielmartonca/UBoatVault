package com.example.uboatvault.api.model.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "Tokens")
public class Token {
    @JsonIgnore
    @Id
    @Getter
    @Setter
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    private Long id;

    @Getter
    @Setter
    @Column(nullable = false, unique = true)
    private String tokenValue;

    @JsonIgnore
    @Getter
    @Setter
    @Column(name = "token_creation_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date tokenCreation;

    @JsonIgnore
    @Getter
    @Setter
    @OneToOne(cascade = CascadeType.ALL, mappedBy = "token")
    private Account account;

    public Token(String tokenValue) {
        this.tokenValue = tokenValue;
        this.tokenCreation = new Date(System.currentTimeMillis());
    }

    @Override
    public String toString() {
        return "Token{" +
                "tokenValue='" + tokenValue + '\'' +
                '}';
    }
}
