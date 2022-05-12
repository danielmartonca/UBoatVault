package com.example.uboatvault.api.model.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Objects;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Entity
@Table(name = "SimCards")
public class SimCard {
    @JsonIgnore
    @Id
    @Getter
    @Setter
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    private Long id;

    @Getter
    @Setter
    private String carrierName;
    @Getter
    @Setter
    private String displayName;
    @Getter
    @Setter
    private Integer slotIndex;
    @Getter
    @Setter
    private String number;
    @Getter
    @Setter
    private String countryIso;
    @Getter
    @Setter
    private String countryPhonePrefix;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "registration_data_id")
    private RegistrationData registrationData;

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
