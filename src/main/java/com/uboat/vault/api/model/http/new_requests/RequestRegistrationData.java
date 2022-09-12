package com.uboat.vault.api.model.http.new_requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestRegistrationData {
    private String deviceInfo;
    private Set<RequestSimCard> mobileNumbersInfoList;
}