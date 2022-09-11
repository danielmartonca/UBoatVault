package com.uboat.vault.api.services;

import com.uboat.vault.api.model.enums.Currency;
import com.uboat.vault.api.model.other.LatLng;
import com.uboat.vault.api.model.persistence.sailing.sailor.Boat;
import com.uboat.vault.api.utilities.GeoUtils;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Date;

@Service
public class GeoService {

    /**
     * TODO
     */
    public Date estimateTimeOfArrival(Timestamp duration) {
        return new Timestamp(System.currentTimeMillis() + duration.getTime());//10 minutes
    }

    /**
     * TODO
     */
    public Timestamp estimateDuration(double totalDistance, Boat boat) {
        return new Timestamp(600000);//10 minutes
    }


    /**
     * TODO
     */
    public Pair<Currency, Double> estimateCost(double totalDistance, Boat boat) {
        return Pair.of(Currency.EUR, Math.random() * 100);
    }

    /**
     * This method calculates the distance between two coordinates.
     * TODO - calculate distance by water. At the moment, the calculation is just the direct geographic distance between the two coordinates
     */
    public double calculateDistanceBetweenCoordinates(LatLng x, LatLng y) {
        return GeoUtils.distanceInMeters(x, y);
    }
}
