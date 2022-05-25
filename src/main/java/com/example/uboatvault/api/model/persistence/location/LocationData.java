package com.example.uboatvault.api.model.persistence.location;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Random;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "LocationData")
@Builder
public class LocationData {
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
    @JoinColumn(name = "journey_id", nullable = false)
    private Journey journey;

    public static LocationData createRandomLocationData() {
        LocationData locationData = new LocationData();

        byte[] array = new byte[7];
        new Random().nextBytes(array);
        locationData.latitude = new String(array, StandardCharsets.UTF_8);
        locationData.longitude = "mock data";
        locationData.accuracy = "mock data";
        locationData.altitude = "mock data";
        locationData.speed = "mock data";
        locationData.speedAccuracy = "mock data";
        locationData.heading = "mock data";
        locationData.time = "mock data";
        locationData.isMock = "mock data";
        locationData.verticalAccuracy = "mock data";
        locationData.headingAccuracy = "mock data";
        locationData.elapsedRealtimeNanos = "mock data";
        locationData.elapsedRealtimeUncertaintyNanos = "mock data";
        locationData.satelliteNumber = "mock data";
        locationData.provider = "mock data";
        return locationData;
    }
}
