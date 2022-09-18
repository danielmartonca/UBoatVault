package com.uboat.vault.api.model.http;

import com.uboat.vault.api.model.persistence.account.info.SimCard;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestSimCard {
    private String carrierName;
    private String displayName;
    private Integer slotIndex;
    private String number;
    private String countryIso;
    private String countryPhonePrefix;

    public RequestSimCard(SimCard simCard) {
        this.carrierName = simCard.getCarrierName();
        this.displayName = simCard.getDisplayName();
        this.slotIndex = simCard.getSlotIndex();
        this.number = simCard.getNumber();
        this.countryIso = simCard.getCountryIso();
        this.countryPhonePrefix = simCard.getCountryPhonePrefix();
    }
}
