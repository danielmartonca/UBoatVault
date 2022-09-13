package com.uboat.vault.api.model.http.new_requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RequestBoat {
    private String type;
    private String model;
    private String licenseNumber;
    private String color;
    private double averageSpeed;
    private String averageSpeedMeasureUnit;
}
