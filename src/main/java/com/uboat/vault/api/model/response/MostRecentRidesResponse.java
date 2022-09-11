package com.uboat.vault.api.model.response;

import com.uboat.vault.api.model.persistence.sailing.Journey;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class MostRecentRidesResponse {
    private List<Journey> rides;
}
