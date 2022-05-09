package com.example.uboatvault.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.*;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Entity
@Table(name = "RegistrationData")
public class RegistrationData {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "registrationDataId", nullable = false, updatable = false)
    @JsonIgnore
    private Long registrationDataId;

    @OneToMany(mappedBy = "registrationData", cascade = CascadeType.ALL,fetch = FetchType.EAGER)
    List<SimCard> mobileNumbersInfoList = new java.util.ArrayList<>();

    @Column(unique = true)
    String deviceInfo;

    @Setter
    @JsonIgnore
    @Column(unique = true)
    String token;

    @Setter
    @JsonIgnore
    @Column(name = "token_creation_date")
    @Temporal(TemporalType.TIMESTAMP)
    Date tokenCreation;

    /*
    Used by jackson to print requests body
     */
    @Override
    public String toString() {
        try {
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            return ow.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }
}
