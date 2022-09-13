package com.uboat.vault.api.model.persistence.account.info;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.uboat.vault.api.model.http.new_requests.RequestCreditCard;
import com.uboat.vault.api.model.persistence.account.Account;
import com.uboat.vault.api.utilities.LoggingUtils;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

import static com.uboat.vault.api.model.persistence.account.info.CreditCard.ValidationStatus.EXPIRED;
import static com.uboat.vault.api.model.persistence.account.info.CreditCard.ValidationStatus.VALID;

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

    public CreditCard(Account account, RequestCreditCard newCreditCard) {
        this.number = newCreditCard.getNumber();
        this.ownerFullName = newCreditCard.getOwnerFullName();
        this.cvc = newCreditCard.getCvc();
        this.expirationDate = newCreditCard.getExpirationDate();

        this.account = account;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CreditCard card = (CreditCard) o;
        return number.equals(card.number) && ownerFullName.equals(card.ownerFullName) && cvc.equals(card.cvc);
    }

    @Override
    public int hashCode() {
        return Objects.hash(number, ownerFullName, cvc);
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
                log.debug("Credit card is not expired.");
                return false;
            }

            log.warn("Credit card is expired. Expiration date is: {}", expirationDate);
            return true;
        } catch (ParseException e) {
            log.debug("Exception occurred while parsing expiration date of the card. Expiration date value: {}", expirationDate, e);
            return false;
        }
    }

    /**
     * Calls all validation methods for the RequestCreditCard given as parameter and returns true the card data are valid.
     */
    public static ValidationStatus validate(RequestCreditCard creditCard) {
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
