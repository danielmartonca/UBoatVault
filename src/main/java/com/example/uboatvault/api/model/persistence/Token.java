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
@Getter
@Entity
@Table(name = "Tokens")
public class Token {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Setter
    @Column(nullable = false, unique = true)
    private String tokenValue;

    @Setter
    @JsonIgnore
    @Column(name = "token_creation_date")
    @Temporal(TemporalType.TIMESTAMP)
    Date tokenCreation;

    public Token(String tokenValue) {
        this.tokenValue = tokenValue;
        this.tokenCreation=new Date(System.currentTimeMillis());
    }

    @OneToOne(cascade = CascadeType.ALL)
    private Account account;

    @Override
    public String toString() {
        return "Token{" +
                "tokenValue='" + tokenValue + '\'' +
                '}';
    }
}
