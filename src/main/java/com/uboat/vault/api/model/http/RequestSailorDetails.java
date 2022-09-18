package com.uboat.vault.api.model.http;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestSailorDetails {
    private String fullName;
    private String phoneNumber;
}
