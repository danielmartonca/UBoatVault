package com.example.uboatvault.api.services;

import com.example.uboatvault.api.model.persistence.location.Journey;
import com.example.uboatvault.api.model.persistence.location.LocationData;
import com.example.uboatvault.api.model.requests.MostRecentRidesRequest;
import com.example.uboatvault.api.repositories.AccountsRepository;
import com.example.uboatvault.api.repositories.JourneyRepository;
import com.example.uboatvault.api.repositories.LocationDataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class JourneyService {
    private final Logger log = LoggerFactory.getLogger(AccountsService.class);

    private final AccountsService accountsService;

    private final AccountsRepository accountsRepository;
    private final JourneyRepository journeyRepository;

    @Autowired
    public JourneyService(AccountsService accountsService, AccountsRepository accountsRepository, JourneyRepository journeyRepository, LocationDataRepository locationDataRepository) {
        this.accountsService = accountsService;
        this.accountsRepository = accountsRepository;
        this.journeyRepository = journeyRepository;
    }

    public List<Journey> getMostRecentRides(String token, MostRecentRidesRequest mostRecentRidesRequest) {
        List<Journey> journeys = new LinkedList<>();
        if (mostRecentRidesRequest.getNrOfRides() <= 0) {
            log.warn("Invalid number of journeys requested: " + mostRecentRidesRequest.getNrOfRides());
            return journeys;
        }


        var foundAccount = accountsService.getAccountByTokenAndCredentials(token, mostRecentRidesRequest.getAccount());
        if (foundAccount == null) {
            log.warn("Credentials are invalid. User is not authorized to receive recent rides data.");
            return null;
        }

        List<Journey> foundJourneys = journeyRepository.findAllByClient_IdAndDateArrivalNotNullOrderByDateBookingAsc(foundAccount.getId());
        if (foundJourneys == null || foundJourneys.isEmpty()) {
            log.warn("User has no journeys.");
            return journeys;
        }

        log.info("Found journeys for the user.");
        for (int i = 0; i < mostRecentRidesRequest.getNrOfRides() && i < foundJourneys.size(); i++) {
            log.info("Added journey " + (i + 1) + " to the response.");
            var journey = foundJourneys.get(i);
            journey.setSailorId(journey.getSailor().getId());
            journey.calculateDuration();
            journeys.add(journey);
        }

        return journeys;
    }

    @Transactional
    public void addFakeJourney(String clientId, String sailorId) {
        var clientAccount = accountsRepository.findById(Long.parseLong(clientId));
        var sailorAccount = accountsRepository.findById(Long.parseLong(sailorId));

        if (clientAccount.isPresent() && sailorAccount.isPresent()) {
            Set<LocationData> locationDataSet = new HashSet<>();
            var locationData = LocationData.createRandomLocationData();
            locationDataSet.add(locationData);
            Journey journey = Journey.builder().client(clientAccount.get()).sailor(sailorAccount.get()).dateBooking(new Date()).dateArrival(new Date()).destination("Milan").source("Source Name").payment("10 EUR").duration("10 minutes").build();
            locationData.setJourney(journey);
            journey.setLocationDataList(locationDataSet);
            journeyRepository.save(journey);
            log.info("Added mock data with success.");
        }
    }
}
