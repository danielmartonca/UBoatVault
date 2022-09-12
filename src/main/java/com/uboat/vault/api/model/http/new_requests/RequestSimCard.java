package com.uboat.vault.api.model.http.new_requests;

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
}
