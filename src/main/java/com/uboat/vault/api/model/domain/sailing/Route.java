package com.uboat.vault.api.model.domain.sailing;

import com.uboat.vault.api.business.services.GeoService;
import com.uboat.vault.api.model.exceptions.NoRouteFoundException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.LinkedList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class Route {
    @AttributeOverrides({
            @AttributeOverride(name = "coordinates.latitude", column = @Column(name = "sourceLatitude")),
            @AttributeOverride(name = "coordinates.longitude", column = @Column(name = "sourceLongitude")),
            @AttributeOverride(name = "address", column = @Column(name = "sourceAddress"))
    })
    @Embedded
    @Getter
    @Setter
    private Location source;

    @AttributeOverrides({
            @AttributeOverride(name = "coordinates.latitude", column = @Column(name = "destinationLatitude")),
            @AttributeOverride(name = "coordinates.longitude", column = @Column(name = "destinationLongitude")),
            @AttributeOverride(name = "address", column = @Column(name = "destinationAddress"))
    })
    @Embedded
    @Getter
    @Setter
    private Location destination;

    @ElementCollection(fetch = FetchType.LAZY)
    private List<LatLng> routePolylinePoints;

    @Setter
    private double totalDistance = 0;

    public Route(Location source, Location destination) {
        this.source = source;
        this.destination = destination;

        this.routePolylinePoints = new LinkedList<>();

        this.totalDistance = 0;
    }

    public void calculateRoute(GeoService geoService, LatLng sailorLocation) throws NoRouteFoundException {
        this.routePolylinePoints.addAll(geoService.calculateOnWaterRouteBetweenCoordinates(sailorLocation, source.getCoordinates(), destination.getCoordinates()));
    }

    public double getTotalDistance() {
        if (totalDistance == 0)
            throw new RuntimeException("Attempted to get Route total distance without it being previously calculated.");
        return totalDistance;
    }

    public double getTotalDistance(GeoService geoService) {
        if (totalDistance != 0) return totalDistance;
        double distance = 0;

        for (int i = 0; i < routePolylinePoints.size() - 1; i++)
            distance += geoService.calculateDistanceBetweenCoordinates(routePolylinePoints.get(i), routePolylinePoints.get(i + 1));

        totalDistance = distance;
        return totalDistance;
    }
}
