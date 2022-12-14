package com.uboat.vault.api.model.domain.sailing;

import com.uboat.vault.api.business.services.GeoService;
import com.uboat.vault.api.model.dto.JourneyRequestDTO;
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
            @AttributeOverride(name = "coordinates.latitude", column = @Column(name = "sailorLocationLatitude")),
            @AttributeOverride(name = "coordinates.longitude", column = @Column(name = "sailorLocationLongitude")),
            @AttributeOverride(name = "address", column = @Column(name = "sailorLocationAddress"))
    })
    @Embedded
    @Getter
    @Setter
    private Location sailorLocation;
    @AttributeOverrides({
            @AttributeOverride(name = "coordinates.latitude", column = @Column(name = "clientLocationLatitude")),
            @AttributeOverride(name = "coordinates.longitude", column = @Column(name = "clientLocationLongitude")),
            @AttributeOverride(name = "address", column = @Column(name = "clientLocationAddress"))
    })
    @Embedded
    @Getter
    @Setter
    private Location clientLocation;

    @AttributeOverrides({
            @AttributeOverride(name = "coordinates.latitude", column = @Column(name = "pickupLocationLatitude")),
            @AttributeOverride(name = "coordinates.longitude", column = @Column(name = "pickupLocationLongitude")),
            @AttributeOverride(name = "address", column = @Column(name = "pickupLocationAddress"))
    })
    @Embedded
    @Getter
    @Setter
    private Location pickupLocation;

    @AttributeOverrides({
            @AttributeOverride(name = "coordinates.latitude", column = @Column(name = "destinationLocationLatitude")),
            @AttributeOverride(name = "coordinates.longitude", column = @Column(name = "destinationLocationLongitude")),
            @AttributeOverride(name = "address", column = @Column(name = "destinationLocationAddress"))
    })
    @Embedded
    @Getter
    @Setter
    private Location destinationLocation;


    @Getter
    @Setter
    @ElementCollection(fetch = FetchType.LAZY)
    private List<LatLng> routePolylinePoints;

    @Setter
    private double totalDistance = 0;

    public Route(Location sailorLocation, Location clientLocation, Location pickupLocation, Location destinationLocation) {
        this.sailorLocation = sailorLocation;
        this.clientLocation = clientLocation;
        this.pickupLocation = pickupLocation;
        this.destinationLocation = destinationLocation;

        this.routePolylinePoints = new LinkedList<>();

        this.totalDistance = 0;
    }

    public Route(Location sailorLocation, JourneyRequestDTO journeyRequestDTO) {
        this(sailorLocation, journeyRequestDTO.getClientLocation(), journeyRequestDTO.getPickupLocation(), journeyRequestDTO.getDestinationLocation());
    }

    public void calculateRoute(GeoService geoService) throws NoRouteFoundException {
        this.routePolylinePoints.addAll(geoService.calculateOnWaterRouteBetweenCoordinates(sailorLocation, pickupLocation, destinationLocation));
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
