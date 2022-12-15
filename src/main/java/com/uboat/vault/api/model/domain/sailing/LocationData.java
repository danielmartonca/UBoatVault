package com.uboat.vault.api.model.domain.sailing;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.uboat.vault.api.model.dto.LocationDataDTO;
import lombok.*;

import javax.persistence.*;
import java.util.Date;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "LocationData")
public class LocationData {
    @ToString.Exclude
    @JsonIgnore
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    private Long id;

    @Getter
    @Setter
    @Column(name = "time_of_recording", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date timeOfRecording;

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

    public LocationData(LocationDataDTO currentLocation) {
        this.timeOfRecording = new Date();
        this.latitude = currentLocation.getLatitude();
        this.longitude = currentLocation.getLongitude();
        this.accuracy = currentLocation.getAccuracy();
        this.altitude = currentLocation.getAltitude();
        this.speed = currentLocation.getSpeed();
        this.speedAccuracy = currentLocation.getSpeedAccuracy();
        this.heading = currentLocation.getHeading();
        this.time = currentLocation.getTime();
        this.isMock = currentLocation.getIsMock();
        this.verticalAccuracy = currentLocation.getVerticalAccuracy();
        this.headingAccuracy = currentLocation.getHeadingAccuracy();
        this.elapsedRealtimeNanos = currentLocation.getElapsedRealtimeNanos();
        this.elapsedRealtimeUncertaintyNanos = currentLocation.getElapsedRealtimeUncertaintyNanos();
        this.satelliteNumber = currentLocation.getSatelliteNumber();
        this.provider = currentLocation.getProvider();
    }

}
