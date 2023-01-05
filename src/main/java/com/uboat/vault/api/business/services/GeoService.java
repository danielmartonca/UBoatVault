package com.uboat.vault.api.business.services;

import com.uboat.vault.api.model.domain.account.sailor.Boat;
import com.uboat.vault.api.model.domain.sailing.LatLng;
import com.uboat.vault.api.model.domain.sailing.Location;
import com.uboat.vault.api.model.enums.Currency;
import com.uboat.vault.api.model.exceptions.NoRouteFoundException;
import com.uboat.vault.api.utilities.GeoUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class GeoService {
    /**
     * TODO
     */
    public int estimateRideDurationInSeconds(double totalDistance, Boat boat) {
        return 100;
    }


    /**
     * TODO
     */
    public Pair<Currency, Double> estimateRideCost(double totalDistance, Boat boat) {
        return Pair.of(Currency.EUR, Math.random() * 100);
    }

    /**
     * This method calculates the distance between two coordinates.
     * TODO - calculate distance by water. At the moment, the calculation is just the direct geographic distance between the two coordinates
     */
    public double calculateDistanceBetweenCoordinates(LatLng x, LatLng y) {
        return GeoUtils.distanceInMeters(x, y);
    }

    /**
     * Calculates the distance between multiple points on water. Make sure all the points are on water and the route is possible before calling this method to get accurate results.
     */
    public double calculateDistanceOnWaterBetweenPoints(List<LatLng> points) {
        double totalDistance = 0;

        for (int i = 0; i < points.size() - 1; i++)
            totalDistance += calculateDistanceBetweenCoordinates(points.get(i), points.get(i + 1));

        return totalDistance;
    }

    public List<LatLng> calculateOnWaterRoute(Location sailorLocation, Location pickupLocation, Location destinationLocation) throws NoRouteFoundException {
        return List.of(sailorLocation.getCoordinates(), pickupLocation.getCoordinates(), destinationLocation.getCoordinates());
    }
}
