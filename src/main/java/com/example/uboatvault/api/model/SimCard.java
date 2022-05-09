package com.example.uboatvault.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Entity
@Table(name = "SimCards")
public class SimCard {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
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
}
