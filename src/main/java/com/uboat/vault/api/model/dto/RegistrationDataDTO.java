package com.uboat.vault.api.model.dto;

import com.uboat.vault.api.model.domain.account.account.RegistrationData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationDataDTO {
    private String deviceInfo;
    private Set<RequestSimCard> mobileNumbersInfoList;

    public RegistrationDataDTO(RegistrationData registrationData) {
        this.deviceInfo = registrationData.getDeviceInfo();
        this.mobileNumbersInfoList = registrationData.getMobileNumbersInfoList().stream().map(RequestSimCard::new).collect(Collectors.toSet());
    }
}