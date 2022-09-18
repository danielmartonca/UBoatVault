package com.uboat.vault.api.model.persistence.account.info;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.uboat.vault.api.model.http.RequestRegistrationData;
import com.uboat.vault.api.model.persistence.account.Account;
import com.uboat.vault.api.utilities.LoggingUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.HashSet;
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

    public RegistrationData(RequestRegistrationData registrationData) {
        this.deviceInfo = registrationData.getDeviceInfo();

        //create new SimCard object for each entry
        this.mobileNumbersInfoList = new HashSet<>(registrationData.getMobileNumbersInfoList().stream().map(SimCard::new).toList());

        //bind each sim card to this registration data
        for (var simCard : mobileNumbersInfoList)
            simCard.setRegistrationData(this);
    }

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
