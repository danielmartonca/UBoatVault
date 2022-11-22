package com.uboat.vault.api.model.domain.account;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.uboat.vault.api.model.domain.account.info.AccountDetails;
import com.uboat.vault.api.model.domain.account.info.CreditCard;
import com.uboat.vault.api.model.domain.account.info.PhoneNumber;
import com.uboat.vault.api.model.domain.account.info.RegistrationData;
import com.uboat.vault.api.model.dto.AccountDTO;
import com.uboat.vault.api.model.enums.UserType;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

@Builder
@AllArgsConstructor
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
    @NotNull
    @Getter
    @Setter
    @Enumerated(EnumType.STRING)
    private UserType type;

    @Getter
    @Setter
    @NotNull
    private String username;
    @Getter
    @NotNull
    @Setter
    private String password;

    @Getter
    @Setter
    @NotNull
    private String email;

    @NotNull
    @Getter
    @Setter
    @OneToOne(cascade = {CascadeType.ALL})
    @JoinColumn(name = "phone_number_id")
    private PhoneNumber phoneNumber;

    @NotNull
    @Getter
    @Setter
    @ManyToOne(cascade = {CascadeType.PERSIST})
    @JoinColumn(name = "registration_data_id", nullable = false)
    private RegistrationData registrationData;

    @Getter
    @Setter
    @OneToOne(cascade = {CascadeType.ALL})
    @JoinColumn(name = "account_details_id")
    private AccountDetails accountDetails;

    @Getter
    @Setter
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CreditCard> creditCards;

    public Account(AccountDTO accountDTO) {
        this.type = accountDTO.getType();

        this.username = accountDTO.getUsername();
        this.password = accountDTO.getPassword();

        //create a new phone number object based on request data
        this.phoneNumber = new PhoneNumber(accountDTO.getPhoneNumber());
        //bind it to the newly created account
        this.phoneNumber.setAccount(this);

        this.email = accountDTO.getEmail();

        this.accountDetails = new AccountDetails(this);

        this.creditCards = new HashSet<>();
    }
}
