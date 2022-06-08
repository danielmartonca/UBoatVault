package com.example.uboatvault.api.model.response;

import com.example.uboatvault.api.model.other.SailorDetails;
import lombok.*;

import java.util.List;


@NoArgsConstructor
@AllArgsConstructor
@Data
public class JourneyResponse {
    private List<SailorDetails> availableSailorsDetails;
}
