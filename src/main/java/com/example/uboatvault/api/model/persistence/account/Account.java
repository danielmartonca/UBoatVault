package com.example.uboatvault.api.model.persistence.account;

import com.example.uboatvault.api.model.enums.UserType;
import com.example.uboatvault.api.model.persistence.account.info.AccountDetails;
import com.example.uboatvault.api.model.persistence.account.info.CreditCard;
import com.example.uboatvault.api.model.persistence.account.info.PhoneNumber;
import com.example.uboatvault.api.model.persistence.account.info.RegistrationData;
import com.example.uboatvault.api.model.persistence.account.pending.PendingAccount;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Set;

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
    @OneToOne(cascade = {CascadeType.ALL})
    @JoinColumn(name = "registration_data_id")
    private RegistrationData registrationData;

    @JsonIgnore
    @Getter
    @Setter
    @OneToOne(cascade = {CascadeType.ALL})
    @JoinColumn(name = "token_id")
    private Token token;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Getter
    @Setter
    @OneToOne(cascade = {CascadeType.ALL})
    @JoinColumn(name = "account_details_id")
    private AccountDetails accountDetails;

    @JsonIgnore
    @Getter
    @Setter
    @OneToMany(mappedBy = "account", cascade = {CascadeType.REMOVE, CascadeType.MERGE})
    private Set<CreditCard> creditCards;

    public boolean equalsPendingAccount(PendingAccount pendingAccount) {
        return this.username.equals(pendingAccount.getUsername()) &&
                this.password.equals(pendingAccount.getPassword());
    }
}
