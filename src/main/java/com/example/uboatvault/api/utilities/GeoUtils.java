package com.example.uboatvault.api.utilities;

import com.example.uboatvault.api.model.other.LatLng;
import com.example.uboatvault.api.model.persistence.sailing.LocationData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.lang.Math.sin;
import static java.lang.Math.pow;
import static java.lang.Math.cos;
import static java.lang.Math.asin;
import static java.lang.Math.sqrt;
import static java.lang.Math.toRadians;

public class GeoUtils {
    private static final Logger log = LoggerFactory.getLogger(GeoUtils.class);

    public static LatLng getCoordinates(LocationData locationData) {
        try {
            double latitude = Double.parseDouble(locationData.getLatitude());
            double longitude = Double.parseDouble(locationData.getLongitude());
            return LatLng.builder().latitude(latitude).longitude(longitude).build();
        } catch (Exception e) {
            log.error("Exception while converting LocationData to LatLng.", e);
            throw e;
        }
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
