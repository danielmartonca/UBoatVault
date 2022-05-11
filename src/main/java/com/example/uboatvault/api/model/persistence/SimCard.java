package com.example.uboatvault.api.model.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Objects;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Entity
@Table(name = "SimCards")
public class SimCard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "simCardId", nullable = false)
    @JsonIgnore
    private Long simCardId;

    String carrierName;
    String displayName;
    Integer slotIndex;
    String number;
    String countryIso;
    String countryPhonePrefix;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "registrationDataId", nullable = false)
    RegistrationData registrationData;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimCard simCard = (SimCard) o;
        return Objects.equals(carrierName, simCard.carrierName) && Objects.equals(displayName, simCard.displayName) && Objects.equals(slotIndex, simCard.slotIndex) && Objects.equals(number, simCard.number) && Objects.equals(countryIso, simCard.countryIso) && Objects.equals(countryPhonePrefix, simCard.countryPhonePrefix);
    }

    @Override
    public int hashCode() {
        return Objects.hash(carrierName, displayName, slotIndex, number, countryIso, countryPhonePrefix);
    }
}
