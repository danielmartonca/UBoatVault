package com.uboat.vault.api.model.persistence.account.info;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.uboat.vault.api.model.http.new_requests.RequestAccountDetails;
import com.uboat.vault.api.model.http.new_requests.RequestImage;
import com.uboat.vault.api.model.persistence.account.Account;
import com.uboat.vault.api.services.AccountsService;
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


    public AccountDetails(Account account) {
        this.account = account;
        this.image = new Image(this);
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

    public void update(RequestAccountDetails newData) {
        if (newData.getFullName() != null && !newData.getFullName().isBlank())
            updateFullName(newData.getFullName());

        if (newData.getEmail() != null && !newData.getEmail().isBlank())
            updateEmail(newData.getEmail());
    }

    public void updateImage(RequestImage newImage) {
        this.image.setBytes(newImage.getBytes());
    }
}
