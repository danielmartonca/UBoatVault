package com.uboat.vault.api.model.response;

import com.uboat.vault.api.model.other.SailorDetails;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@NoArgsConstructor
@AllArgsConstructor
@Data
public class JourneyResponse {
    private List<SailorDetails> availableSailorsDetails;
}
