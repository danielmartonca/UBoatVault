package com.uboat.vault.api.model.persistence.sailing.sailor;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.uboat.vault.api.model.http.RequestBoat;
import lombok.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.util.LinkedHashSet;
import java.util.Set;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "Boats")
public class Boat {
    private static final Logger log = LoggerFactory.getLogger(Boat.class);

    @JsonIgnore
    @Id
    @Getter
    @Setter
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    private Long id;

    @Getter
    @Setter
    private String type;

    @Getter
    @Setter
    private String model;

    @Getter
    @Setter
    private String licenseNumber;

    @Getter
    @Setter
    private String color;

    @Getter
    @Setter
    private double averageSpeed;

    @Getter
    @Setter
    private String averageSpeedMeasureUnit;

    @JsonIgnore
    @Getter
    @Setter
    @OneToOne(mappedBy = "boat", cascade = CascadeType.MERGE)
    private Sailor sailor;

    @JsonIgnore
    @Getter
    @Setter
    @OneToMany(mappedBy = "boat", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<BoatImage> boatImages;

    public Boat(Sailor sailor) {
        this.sailor = sailor;
        this.averageSpeed = 0;
        this.type = "";
        this.boatImages = new LinkedHashSet<>();
    }


    private void updateBoatType(String newValue) {
        this.type = newValue;
        log.info("Updated boat type.");
    }

    private void updateBoatModel(String newValue) {
        this.model = newValue;
        log.info("Updated boat model.");
    }

    private void updateBoatLicenseNumber(String newValue) {
        this.licenseNumber = newValue;
        log.info("Updated boat license number.");
    }

    private void updateBoatColor(String newValue) {
        this.color = newValue;
        log.info("Updated boat color.");
    }

    private void updateBoatAverageSpeed(double newValue) {
        this.averageSpeed = newValue;
        log.info("Updated boat average speed.");
    }

    private void updateBoatAverageSpeedMeasureUnit(String newValue) {
        this.averageSpeedMeasureUnit = newValue;
        log.info("Updated boat average speed measure unit.");
    }

    public void update(RequestBoat boat) {
        if (boat.getType() != null && !boat.getType().isEmpty())
            updateBoatType(boat.getType());

        if (boat.getModel() != null && !boat.getModel().isEmpty())
            updateBoatModel(boat.getModel());

        if (boat.getLicenseNumber() != null && !boat.getLicenseNumber().isEmpty())
            updateBoatLicenseNumber(boat.getLicenseNumber());

        if (boat.getColor() != null && !boat.getColor().isEmpty())
            updateBoatColor(boat.getColor());

        if (boat.getAverageSpeed() > 0)
            updateBoatAverageSpeed(boat.getAverageSpeed());

        if (boat.getAverageSpeedMeasureUnit() != null && !boat.getAverageSpeedMeasureUnit().isEmpty())
            updateBoatAverageSpeedMeasureUnit(boat.getAverageSpeedMeasureUnit());
    }
}
