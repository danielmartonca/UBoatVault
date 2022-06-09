package com.example.uboatvault.api.model.persistence.account.info;

import com.example.uboatvault.api.model.persistence.account.Account;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.*;

import javax.persistence.*;
import java.util.Objects;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "RegistrationData")
public class RegistrationData {
    @JsonIgnore
    @Id
    @Getter
    @Setter
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    private Long id;

    @Getter
    @Setter
    @OneToMany(mappedBy = "registrationData", cascade = CascadeType.ALL)
    private Set<SimCard> mobileNumbersInfoList;

    @Getter
    @Setter
    private String deviceInfo;

    @JsonIgnore
    @Getter
    @Setter
    @OneToOne(cascade = CascadeType.ALL, optional = false, mappedBy = "registrationData")
    private Account account;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RegistrationData that = (RegistrationData) o;
        return Objects.equals(mobileNumbersInfoList, that.mobileNumbersInfoList) && Objects.equals(deviceInfo, that.deviceInfo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mobileNumbersInfoList, deviceInfo);
    }

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
