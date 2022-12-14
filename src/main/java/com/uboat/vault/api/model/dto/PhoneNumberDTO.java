package com.uboat.vault.api.model.dto;

import com.uboat.vault.api.model.domain.account.account.Phone;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhoneNumberDTO {
    @NotNull
    private String number;
    @NotNull
    private String dialCode;
    @NotNull
    private String isoCode;

    public PhoneNumberDTO(Phone phone) {
        this.number = phone.getNumber();
        this.dialCode = phone.getDialCode();
        this.isoCode = phone.getIsoCode();
    }
}
