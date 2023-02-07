package com.uboat.vault.api.business.services.geographical;

import com.uboat.vault.api.model.domain.account.sailor.Boat;
import com.uboat.vault.api.model.domain.sailing.LatLng;
import com.uboat.vault.api.model.domain.sailing.Location;
import com.uboat.vault.api.model.enums.Currency;
import com.uboat.vault.api.model.exceptions.NoRouteFoundException;
import com.uboat.vault.api.utilities.GeoUtils;
import org.springframework.data.util.Pair;

import java.util.List;

public interface GeographicalService {
    int estimateRideDurationInSeconds(double totalDistance, Boat boat);

    Pair<Currency, Double> estimateRideCost(double totalDistance, Boat boat);

    List<LatLng> calculateOnWaterRoute(Location sailorLocation, Location pickupLocation, Location destinationLocation) throws NoRouteFoundException;

    /**
     * This method calculates the distance between two coordinates.
     * TODO - calculate distance by water. At the moment, the calculation is just the direct geographic distance between the two coordinates
     */
    default double calculateDistanceBetweenCoordinates(LatLng x, LatLng y) {
        return GeoUtils.distanceInMeters(x, y);
    }

    default double calculateDistanceOnWaterBetweenPoints(List<LatLng> points) {
        {
            double totalDistance = 0;

            for (int i = 0; i < points.size() - 1; i++)
                totalDistance += calculateDistanceBetweenCoordinates(points.get(i), points.get(i + 1));

            return totalDistance;
        }
    }
}
