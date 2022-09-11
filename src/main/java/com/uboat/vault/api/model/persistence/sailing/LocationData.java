package com.uboat.vault.api.model.persistence.sailing;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;
import java.util.Random;

@ToString()
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "LocationData")
@Builder
public class LocationData {
    @ToString.Exclude
    @JsonIgnore
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    private Long id;

    private String latitude;
    private String longitude;
    private String accuracy;
    private String altitude;
    private String speed;
    private String speedAccuracy;
    private String heading;
    private String time;
    private String isMock;
    private String verticalAccuracy;
    private String headingAccuracy;
    private String elapsedRealtimeNanos;
    private String elapsedRealtimeUncertaintyNanos;
    private String satelliteNumber;
    private String provider;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "journey_id")
    private Journey journey;

    @Transient
    private static Random rnd = new Random();

    public static LocationData createRandomLocationData() {
        LocationData locationData = new LocationData();
        locationData.latitude = String.valueOf(rnd.nextInt() * 100);
        locationData.longitude = "1";
        locationData.accuracy = "1";
        locationData.altitude = "1";
        locationData.speed = "1";
        locationData.speedAccuracy = "1";
        locationData.heading = "1";
        locationData.time = "1";
        locationData.isMock = "0";
        locationData.verticalAccuracy = "1";
        locationData.headingAccuracy = "1";
        locationData.elapsedRealtimeNanos = "1";
        locationData.elapsedRealtimeUncertaintyNanos = "1";
        locationData.satelliteNumber = "1";
        locationData.provider = "1";
        return locationData;
    }
}
