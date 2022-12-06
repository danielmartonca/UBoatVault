package com.uboat.vault.api.model.domain.account.account;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.uboat.vault.api.business.services.AccountsService;
import com.uboat.vault.api.model.dto.AccountDTO;
import com.uboat.vault.api.model.dto.AccountDetailsDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "AccountsDetails")
public class AccountDetails {
    private static final Logger log = LoggerFactory.getLogger(AccountsService.class);

    @JsonIgnore
    @Id
    @Getter
    @Setter
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    private Long id;

    @Getter
    @Setter
    private String fullName;

    @Getter
    @Setter
    private String email;


    @JsonIgnore
    @Getter
    @Setter
    @OneToOne(mappedBy = "accountDetails", orphanRemoval = true)
    private Account account;

    @Getter
    @Setter
    @OneToOne(cascade = CascadeType.ALL, mappedBy = "accountDetails")
    private Image image;


    public AccountDetails(AccountDTO accountDTO, Account accountRef) {
        this.account = accountRef;
        this.image = new Image(this);
        this.email=accountDTO.getEmail();
    }

    //the next methods control the behaviour of updates. This way adding email verification will be easier in the future
    private void updateFullName(String newFullName) {
        this.fullName = newFullName;
        log.info("Full name of the account was updated.");
    }

    private void updateEmail(String newEmail) {
        this.email = newEmail;
        log.info("Email of the account was updated.");
    }

    public void update(AccountDetailsDTO newData) {
        if (newData.getFullName() != null && !newData.getFullName().isBlank())
            updateFullName(newData.getFullName());

        if (newData.getEmail() != null && !newData.getEmail().isBlank())
            updateEmail(newData.getEmail());
    }

}
