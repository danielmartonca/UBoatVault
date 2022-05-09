package com.example.uboatvault.api.model.persistence;

import com.example.uboatvault.api.services.EncryptionService;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@NoArgsConstructor
@Getter
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

    @Setter
    private String token;
    @NotNull
    @Setter
    private String phoneNumber;
    @Setter
    private String username;
    @Setter
    private String name;
    @Setter
    private String surname;
    @NotNull
    @Setter
    private String password;

    @NotNull
    @OneToOne
    @JoinColumn
    private RegistrationData registrationData;

    public Account(String token, String phoneNumber, String username, String name, String surname, String password, RegistrationData registrationData) {
        this.token = token;
        this.phoneNumber = phoneNumber;
        this.name = name;
        this.surname = surname;
        this.username = encryptionService.encryptString(username);
        this.password = encryptionService.encryptString(password);
        this.registrationData = registrationData;
    }

    private void decryptCredentials() {
        this.username = encryptionService.decryptString(this.username);
        this.password = encryptionService.decryptString(this.password);
    }

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
}
