package com.uboat.vault.api.model.persistence.account;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.uboat.vault.api.model.enums.UserType;
import com.uboat.vault.api.model.http.RequestAccount;
import com.uboat.vault.api.model.persistence.account.info.AccountDetails;
import com.uboat.vault.api.model.persistence.account.info.CreditCard;
import com.uboat.vault.api.model.persistence.account.info.PhoneNumber;
import com.uboat.vault.api.model.persistence.account.info.RegistrationData;
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
    @OneToMany(mappedBy = "account", cascade = {CascadeType.REMOVE, CascadeType.MERGE})
    private Set<CreditCard> creditCards;

    public Account(RequestAccount requestAccount) {
        this.type = requestAccount.getType();

        this.username = requestAccount.getUsername();
        this.password = requestAccount.getPassword();

        //create a new phone number object based on request data
        this.phoneNumber = new PhoneNumber(requestAccount.getPhoneNumber());
        //bind it to the newly created account
        this.phoneNumber.setAccount(this);

        this.accountDetails = new AccountDetails(this);

        this.creditCards = new HashSet<>();
    }
}
