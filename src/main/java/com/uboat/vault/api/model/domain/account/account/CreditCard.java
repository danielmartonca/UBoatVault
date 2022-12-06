package com.uboat.vault.api.model.domain.account.account;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.uboat.vault.api.model.dto.CreditCardDTO;
import com.uboat.vault.api.utilities.LoggingUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.uboat.vault.api.model.domain.account.account.CreditCard.ValidationStatus.EXPIRED;
import static com.uboat.vault.api.model.domain.account.account.CreditCard.ValidationStatus.VALID;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "CreditCards")
public class CreditCard {
    private static final Logger log = LoggerFactory.getLogger(CreditCard.class);

    @JsonIgnore
    @Id
    @Getter
    @Setter
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    private Long id;

    @Getter
    @Setter
    private String number;

    @Getter
    @Setter
    private String ownerFullName;

    @Getter
    @Setter
    private String cvc;

    @Getter
    @Setter
    private String expirationDate;

    @JsonIgnore
    @Getter
    @Setter
    @ManyToOne()
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    public CreditCard(Account account, CreditCardDTO newCreditCard) {
        this.number = newCreditCard.getNumber();
        this.ownerFullName = newCreditCard.getOwnerFullName();
        this.cvc = newCreditCard.getCvc();
        this.expirationDate = newCreditCard.getExpirationDate();

        this.account = account;
    }

    public boolean equalsRequestCard(CreditCardDTO creditCardDTO) {
        return this.number.equals(creditCardDTO.getNumber()) && this.ownerFullName.equals(creditCardDTO.getOwnerFullName());
    }

    @Override
    public String toString() {
        return LoggingUtils.toStringFormatted(this);
    }

    private static boolean isExpired(String expirationDate) {
        try {
            var dateFormat = new SimpleDateFormat("MM/yy");
            var cardDate = dateFormat.parse(expirationDate);

            if (new Date().compareTo(cardDate) > 0) {
                log.warn("Credit card is expired. Expiration date is: {}", expirationDate);
                return true;
            }

            log.debug("Credit card is not expired.");
            return false;
        } catch (ParseException e) {
            log.debug("Exception occurred while parsing expiration date of the card. Expiration date value: {}", expirationDate, e);
            return false;
        }
    }

    /**
     * Calls all validation methods for the RequestCreditCard given as parameter and returns true the card data are valid.
     */
    public static ValidationStatus validate(CreditCardDTO creditCard) {
        if (isExpired(creditCard.getExpirationDate())) {
            log.warn("Credit card is not valid.");
            return EXPIRED;
        }

        log.info("Credit card is valid.");
        return VALID;
    }

    public enum ValidationStatus {
        VALID, EXPIRED
    }
}
