package com.uboat.vault.api.model.domain.account.account;

import com.uboat.vault.api.model.dto.PhoneNumberDTO;
import com.uboat.vault.api.utilities.LoggingUtils;
import lombok.*;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Embeddable
@EqualsAndHashCode
public class Phone {
    @Getter
    @Setter
    @Column(nullable = false)
    private String number;
    @Getter
    @Setter
    @Column(nullable = false)
    private String dialCode;
    @Getter
    @Setter
    @Column(nullable = false)
    private String isoCode;

    public Phone(PhoneNumberDTO phoneNumber) {
        this.number = phoneNumber.getPhoneNumber();
        this.dialCode = phoneNumber.getDialCode();
        this.isoCode = phoneNumber.getIsoCode();
    }

    @Override
    public String toString() {
        return LoggingUtils.toStringFormatted(this);
    }
}
