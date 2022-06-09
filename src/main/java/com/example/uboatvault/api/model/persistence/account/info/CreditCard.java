package com.example.uboatvault.api.model.persistence.account.info;

import com.example.uboatvault.api.model.persistence.account.Account;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "CreditCards")
public class CreditCard {
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

    @JsonIgnore
    public boolean isExpired() throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/yy");
        var cardDate = dateFormat.parse(expirationDate);
        Date dateNow = new Date();
        int comparison = dateNow.compareTo(cardDate);
        return comparison > 0;
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
}
