package com.uboat.vault.api.model.dto;

import com.uboat.vault.api.model.domain.account.account.SimCard;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimCardDTO {
    private String carrierName;
    private String displayName;
    private Integer slotIndex;
    private String number;
    private String countryIso;
    private String countryPhonePrefix;

    public SimCardDTO(SimCard simCard) {
        this.carrierName = simCard.getCarrierName();
        this.displayName = simCard.getDisplayName();
        this.slotIndex = simCard.getSlotIndex();
        this.number = simCard.getNumber();
        this.countryIso = simCard.getCountryIso();
        this.countryPhonePrefix = simCard.getCountryPhonePrefix();
    }
}
