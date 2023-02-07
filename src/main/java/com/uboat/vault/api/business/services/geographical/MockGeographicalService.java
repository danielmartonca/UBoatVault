package com.uboat.vault.api.business.services.geographical;

import com.uboat.vault.api.model.domain.account.sailor.Boat;
import com.uboat.vault.api.model.domain.sailing.LatLng;
import com.uboat.vault.api.model.domain.sailing.Location;
import com.uboat.vault.api.model.enums.Currency;
import com.uboat.vault.api.model.exceptions.NoRouteFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Profile("development")
public class MockGeographicalService implements GeographicalService {

    @Override
    public int estimateRideDurationInSeconds(double totalDistance, Boat boat) {
        return 100;
    }


    @Override
    public Pair<Currency, Double> estimateRideCost(double totalDistance, Boat boat) {
        return Pair.of(Currency.EUR, Math.random() * 100);
    }

    @Override
    public List<LatLng> calculateOnWaterRoute(Location sailorLocation, Location pickupLocation, Location destinationLocation) throws NoRouteFoundException {
        return List.of(sailorLocation.getCoordinates(), pickupLocation.getCoordinates(), destinationLocation.getCoordinates());
    }
}
