package com.uboat.vault.api.model.other;

import lombok.*;

@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class LatLng {
    private double latitude;
    private double longitude;
}