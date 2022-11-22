package com.uboat.vault.api.model.domain.account.info;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.uboat.vault.api.model.domain.account.Account;
import com.uboat.vault.api.model.dto.PhoneNumberDTO;
import com.uboat.vault.api.utilities.LoggingUtils;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Objects;

@NoArgsConstructor
@Getter
@Entity
@Table(name = "PhoneNumbers")
public class PhoneNumber {
    @JsonIgnore
    @Id
    @Getter
    @Setter
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    private Long id;

    @Getter
    @Setter
    @Column(nullable = false)
    private String phoneNumber;
    @Getter
    @Setter
    @Column(nullable = false)
    private String dialCode;
    @Getter
    @Setter
    @Column(nullable = false)
    private String isoCode;

    @JsonIgnore
    @Getter
    @Setter
    @OneToOne(mappedBy = "phoneNumber", cascade = CascadeType.MERGE)
    private Account account;

    public PhoneNumber(PhoneNumberDTO phoneNumber,Account accountRef) {
        this.phoneNumber = phoneNumber.getPhoneNumber();
        this.dialCode = phoneNumber.getDialCode();
        this.isoCode = phoneNumber.getIsoCode();
        this.account=accountRef;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PhoneNumber that = (PhoneNumber) o;
        return phoneNumber.equals(that.phoneNumber) && dialCode.equals(that.dialCode) && isoCode.equals(that.isoCode);
    }

    public boolean equals(PhoneNumberDTO phoneNumber) {
        return this.phoneNumber.equals(phoneNumber.getPhoneNumber())
                && this.dialCode.equals(phoneNumber.getDialCode())
                && this.isoCode.equals(phoneNumber.getIsoCode());
    }

    @Override
    public int hashCode() {
        return Objects.hash(phoneNumber, dialCode, isoCode);
    }

    @Override
    public String toString() {
        return LoggingUtils.toStringFormatted(this);
    }
}
