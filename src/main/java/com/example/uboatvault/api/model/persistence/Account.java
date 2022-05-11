package com.example.uboatvault.api.model.persistence;

import com.example.uboatvault.api.services.EncryptionService;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

@NoArgsConstructor
@Entity
@Table(name = "Accounts")
public class Account {
    @Autowired
    @Transient
    @JsonIgnore
    EncryptionService encryptionService;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    @Getter
    @NotNull
    @Setter
    @OneToOne(cascade = {CascadeType.ALL})
    @JoinColumn
    private PhoneNumber phoneNumber;

    @Getter
    @Setter
    private String username;

    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private String surname;

    @Getter
    @NotNull
    @Setter
    private String password;

    @Getter
    @Setter
    @OneToOne(cascade = {CascadeType.ALL})
    @JoinColumn(unique = true)
    private Token token;

    @Getter
    @NotNull
    @OneToOne(cascade = {CascadeType.ALL})
    @JoinColumn
    private RegistrationData registrationData;

    @Override
    public String toString() {
        return "RegistrationRequest{" +
                "token='" + token + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", username='" + username + '\'' +
                ", name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", password='" + "***" + '\'' +
                ", registrationData=" + registrationData +
                '}';
    }

    public boolean equalsPendingAccount(PendingAccount pendingAccount) {
        return this.username.equals(pendingAccount.getUsername()) &&
                this.password.equals(pendingAccount.getPassword()) ;
    }
}
