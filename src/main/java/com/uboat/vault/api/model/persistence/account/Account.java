package com.uboat.vault.api.model.persistence.account;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.uboat.vault.api.model.enums.UserType;
import com.uboat.vault.api.model.persistence.account.info.AccountDetails;
import com.uboat.vault.api.model.persistence.account.info.CreditCard;
import com.uboat.vault.api.model.persistence.account.info.PhoneNumber;
import com.uboat.vault.api.model.persistence.account.info.RegistrationData;
import com.uboat.vault.api.model.persistence.account.pending.PendingAccount;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
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
