package com.uboat.vault.api.model.http.new_requests;

import com.uboat.vault.api.model.persistence.account.info.RegistrationData;
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
public class RequestRegistrationData {
    private String deviceInfo;
    private Set<RequestSimCard> mobileNumbersInfoList;

    public RequestRegistrationData(RegistrationData registrationData) {
        this.deviceInfo = registrationData.getDeviceInfo();
        this.mobileNumbersInfoList = registrationData.getMobileNumbersInfoList().stream().map(RequestSimCard::new).collect(Collectors.toSet());
    }
}