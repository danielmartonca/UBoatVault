package com.example.uboatvault.api.model.persistence.account.info;

import com.example.uboatvault.api.model.persistence.account.Account;
import com.example.uboatvault.api.utilities.LoggingUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;
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
    @OneToMany(mappedBy = "registrationData", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Account> accounts;

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

    @Override
    public String toString() {
        return LoggingUtils.toStringFormatted(this);
    }
}
