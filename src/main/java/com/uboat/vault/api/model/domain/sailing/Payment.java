package com.uboat.vault.api.model.domain.sailing;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.uboat.vault.api.model.domain.account.account.CreditCard;
import com.uboat.vault.api.model.enums.Currency;
import com.uboat.vault.api.model.enums.PaymentType;
import lombok.*;
import org.springframework.data.util.Pair;

import javax.persistence.*;
import javax.validation.constraints.DecimalMin;
import java.util.Date;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "Payments")
public class Payment {
    @JsonIgnore
    @Id
    @Getter
    @Setter
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Getter
    @Setter
    private PaymentType paymentType;

    @Enumerated(EnumType.STRING)
    @Getter
    @Setter
    private Currency currency;

    @DecimalMin("1.00")
    @Getter
    @Setter
    private double amount;

    //only applies if paymentType is CARD
    @Getter
    @Setter
    @ManyToOne()
    @JoinColumn(name = "credit_card_id")
    private CreditCard creditCard;

    @Getter
    @Setter
    private boolean completed = false;

    @Temporal(TemporalType.TIMESTAMP)
    @Getter
    @Setter
    private Date timeOfCompletion;

    @Getter
    @Setter
    @OneToOne(cascade = {CascadeType.ALL})
    @JoinColumn(name = "journeyId", nullable = false, unique = true, updatable = false)
    private Journey journey;

    public Payment(Journey journey, Pair<Currency, Double> paymentPair) {
        this.currency = paymentPair.getFirst();
        this.amount = paymentPair.getSecond();
        this.completed = false;
        this.journey = journey;
    }

    public void complete() {
        completed = true;
        timeOfCompletion = new Date();
    }
}
