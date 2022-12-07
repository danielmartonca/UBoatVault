package com.uboat.vault.api.model.dto;

import com.uboat.vault.api.model.domain.account.sailor.Boat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BoatDTO {
    private String type;
    private String model;
    private String licenseNumber;
    private String color;
    private double averageSpeed;
    private String averageSpeedMeasureUnit;

    public BoatDTO(Boat boat) {
        this.type = boat.getType();
        this.model = boat.getModel();
        this.licenseNumber = boat.getLicenseNumber();
        this.color = boat.getColor();
        this.averageSpeed = boat.getAverageSpeed();
        this.averageSpeedMeasureUnit = boat.getAverageSpeedMeasureUnit();
    }
}
