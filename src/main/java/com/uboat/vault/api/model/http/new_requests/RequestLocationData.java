package com.uboat.vault.api.model.http.new_requests;

import com.uboat.vault.api.model.persistence.sailing.LocationData;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RequestLocationData {
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

    public RequestLocationData(LocationData locationData) {
        this.latitude = locationData.getLatitude();
        this.longitude = locationData.getLongitude();
        this.accuracy = locationData.getAccuracy();
        this.altitude = locationData.getAltitude();
        this.speed = locationData.getSpeed();
        this.speedAccuracy = locationData.getSpeedAccuracy();
        this.heading = locationData.getHeading();
        this.time = locationData.getTime();
        this.isMock = locationData.getIsMock();
        this.verticalAccuracy = locationData.getVerticalAccuracy();
        this.headingAccuracy = locationData.getHeadingAccuracy();
        this.elapsedRealtimeNanos = locationData.getElapsedRealtimeNanos();
        this.elapsedRealtimeUncertaintyNanos = locationData.getElapsedRealtimeUncertaintyNanos();
        this.satelliteNumber = locationData.getSatelliteNumber();
        this.provider = locationData.getProvider();
    }
}
