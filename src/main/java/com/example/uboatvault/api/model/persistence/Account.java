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
@Table(name = "Accounts")
public class Account {
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
    @NotNull
    private String username;

    @Getter
    @NotNull
    @Setter
    private String password;

    @NotNull
    @Getter
    @Setter
    @OneToOne(cascade = {CascadeType.ALL})
    @JoinColumn(name = "phone_number_id")
    private PhoneNumber phoneNumber;

    @NotNull
    @Getter
    @Setter
    @OneToOne(cascade = {CascadeType.ALL})
    @JoinColumn(name = "registration_data_id")
    private RegistrationData registrationData;

    @JsonIgnore
    @Getter
    @Setter
    @OneToOne(cascade = {CascadeType.ALL})
    @JoinColumn(name = "token_id")
    private Token token;

    @Getter
    @Setter
    @OneToOne(cascade = {CascadeType.ALL})
    @JoinColumn(name = "account_details_id")
    private AccountDetails accountDetails;


    public boolean equalsPendingAccount(PendingAccount pendingAccount) {
        return this.username.equals(pendingAccount.getUsername()) &&
                this.password.equals(pendingAccount.getPassword());
    }
}
