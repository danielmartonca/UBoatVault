package com.uboat.vault.api.model.persistence.sailing;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.uboat.vault.api.model.http.new_requests.RequestLocationData;
import com.uboat.vault.api.utilities.LoggingUtils;
import lombok.*;

import javax.persistence.*;
import java.util.Date;
import java.util.Random;

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
    @Transient
    private static Random rnd = new Random();

    public LocationData(RequestLocationData currentLocation) {
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

    public static LocationData createRandomLocationData() {
        LocationData locationData = new LocationData();
        locationData.timeOfRecording = new Date();
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

    @Override
    public String toString() {
        return LoggingUtils.toStringFormatted(this);
    }
}
