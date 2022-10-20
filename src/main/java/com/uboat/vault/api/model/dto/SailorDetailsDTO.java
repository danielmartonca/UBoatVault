package com.uboat.vault.api.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SailorDetailsDTO {
    private String fullName;
    private String phoneNumber;
}
