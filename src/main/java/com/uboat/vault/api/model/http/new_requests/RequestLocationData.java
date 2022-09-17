package com.uboat.vault.api.model.http.new_requests;

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
}
