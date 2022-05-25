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
        locationData.latitude = String.valueOf(new Random().nextInt() * 100);
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
