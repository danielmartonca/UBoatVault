package com.example.uboatvault.api.model.persistence.sailing.sailor;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;
import java.util.Set;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "Boats")
public class Boat {
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
    private ActiveSailor sailor;

    @Getter
    @Setter
    @OneToMany(mappedBy = "boat", cascade = CascadeType.ALL)
    private Set<BoatImage> boatImages;
}
