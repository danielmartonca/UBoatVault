package com.uboat.vault.api.utilities;

import com.uboat.vault.api.model.domain.sailing.LocationData;
import com.uboat.vault.api.model.dto.LocationDataDTO;
import com.uboat.vault.api.model.other.LatLng;
import lombok.extern.slf4j.Slf4j;

import static java.lang.Math.*;

@Slf4j
public class GeoUtils {
    public static LatLng getCoordinates(LocationDataDTO locationData) {
        try {
            double latitude = Double.parseDouble(locationData.getLatitude());
            double longitude = Double.parseDouble(locationData.getLongitude());
            return LatLng.builder().latitude(latitude).longitude(longitude).build();
        } catch (Exception e) {
            log.error("Exception while converting LocationData to LatLng.", e);
            throw e;
        }
    }

    public static LatLng getCoordinates(LocationData locationData) {
       return getCoordinates(new LocationDataDTO(locationData));
    }

    public static double distanceInMeters(LatLng x, LatLng y) {
        var lon1 = toRadians(x.getLongitude());
        var lon2 = toRadians(y.getLongitude());
        var lat1 = toRadians(x.getLatitude());
        var lat2 = toRadians(y.getLatitude());

        // Haversine formula
        double longitude = lon2 - lon1;
        double latitude = lat2 - lat1;
        double a = pow(sin(latitude / 2), 2) + cos(lat1) * cos(lat2) * pow(sin(longitude / 2), 2);

        double c = 2 * asin(sqrt(a));
        double r = 6371; //Kilometers
        return (c * r * 1000); //meters
    }
}
