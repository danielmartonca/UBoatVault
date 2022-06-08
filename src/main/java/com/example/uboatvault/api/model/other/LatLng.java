package com.example.uboatvault.api.model.other;

import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class LatLng {
    private double latitude;
    private double longitude;
}