package com.example.uboatvault.api.model.response;

import com.example.uboatvault.api.model.persistence.location.Journey;
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
