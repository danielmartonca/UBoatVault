package com.example.uboatvault.api.services;

import com.example.uboatvault.api.model.enums.UserType;
import com.example.uboatvault.api.model.other.LatLng;
import com.example.uboatvault.api.model.other.SailorDetails;
import com.example.uboatvault.api.model.persistence.sailing.sailor.ActiveSailor;
import com.example.uboatvault.api.model.persistence.sailing.Journey;
import com.example.uboatvault.api.model.persistence.sailing.LocationData;
import com.example.uboatvault.api.model.requests.JourneyRequest;
import com.example.uboatvault.api.model.requests.MostRecentRidesRequest;
import com.example.uboatvault.api.model.response.JourneyResponse;
import com.example.uboatvault.api.repositories.AccountsRepository;
import com.example.uboatvault.api.repositories.ActiveSailorsRepository;
import com.example.uboatvault.api.repositories.JourneyRepository;
import com.example.uboatvault.api.repositories.LocationDataRepository;
import com.example.uboatvault.api.utility.logging.GeoUtils;
import com.example.uboatvault.api.utility.logging.ListUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class JourneyService {
    private final Logger log = LoggerFactory.getLogger(JourneyService.class);

    private final int MAX_ACTIVE_SAILORS = 5;
    private final int MAX_ACTIVE_SECONDS = 300;

    private final AccountsService accountsService;
    private final GeoService geoService;

    private final AccountsRepository accountsRepository;
    private final JourneyRepository journeyRepository;
    private final ActiveSailorsRepository activeSailorsRepository;

    @Autowired
    public JourneyService(AccountsService accountsService, GeoService geoService, AccountsRepository accountsRepository, JourneyRepository journeyRepository, LocationDataRepository locationDataRepository, ActiveSailorsRepository activeSailorsRepository) {
        this.accountsService = accountsService;
        this.geoService = geoService;
        this.accountsRepository = accountsRepository;
        this.journeyRepository = journeyRepository;
        this.activeSailorsRepository = activeSailorsRepository;
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

    /**
     * This method returns a list of Pairs < ActiveSailor, distance > where each pair represents
     * the distance from the ActiveSailor's coordinates to the destinationCoordinates given as parameter
     */
    private List<Pair<ActiveSailor, Double>> getDistanceToCoordinatesList(List<ActiveSailor> activeSailors, LatLng destinationCoordinates) {
        List<Pair<ActiveSailor, Double>> activeSailorsDistanceList = new LinkedList<>();
        for (var activeSailor : activeSailors) {
            try {
                LatLng sailorCoordinates = GeoUtils.getCoordinates(activeSailor.getLocationData());

                double distanceToDestination = geoService.calculateDistanceBetweenCoordinates(sailorCoordinates, destinationCoordinates);
                if (distanceToDestination < 0) {
                    log.error("Distance calculated was negative value '" + distanceToDestination + "' for active sailor:\n" + activeSailor);
                    continue;
                }

                var pair = Pair.of(activeSailor, distanceToDestination);
                activeSailorsDistanceList.add(pair);
            } catch (Exception e) {
                log.error("Exception during distance calculation workflow for active sailor:\n" + activeSailor);
            }
        }
        activeSailorsDistanceList.sort(Comparator.comparingLong(pair -> pair.getFirst().getAccountId()));
        return activeSailorsDistanceList;
    }

    /**
     * This method takes two lists of pairs calculated with {@link #getDistanceToCoordinatesList(List, LatLng)}
     * and sums pairs that match ActiveSailor objects by accountId.
     * <br>If there are pairs in any of the two lists where the ActiveSailor does not have a correspondence in the other list, they will be completely ignored by the algorithm.
     * <p><br>
     * This method returns a list of pairs sorted by ActiveSailor id!
     */
    private List<Pair<ActiveSailor, Double>> sumListsOfDistances(List<Pair<ActiveSailor, Double>> list1, List<Pair<ActiveSailor, Double>> list2) {
        List<Pair<ActiveSailor, Double>> totalDistanceList = new LinkedList<>();

        for (int i = 0; i < Math.min(list1.size(), list2.size()); i++) {
            var pair1 = list1.get(i);
            var pair2 = list2.get(i);

            if (pair1.getFirst().getAccountId().equals(pair2.getFirst().getAccountId())) {
                totalDistanceList.add(Pair.of(pair1.getFirst(), pair1.getSecond() + pair2.getSecond()));
            } else {
                Pair<ActiveSailor, Double> pair;
                List<Pair<ActiveSailor, Double>> searchList;
                if (pair1.getFirst().getAccountId() >= pair2.getFirst().getAccountId()) {
                    pair = pair1;
                    searchList = list2;
                } else {
                    pair = pair2;
                    searchList = list1;
                }
                Long sailorAccountId = pair.getFirst().getAccountId();
                var foundPair = ListUtilities.findPairByActiveSailorAccountId(sailorAccountId, searchList, i + 1);
                if (foundPair != null)
                    totalDistanceList.add(Pair.of(pair.getFirst(), pair.getSecond() + foundPair.getSecond()));
            }
        }

        return totalDistanceList;
    }

    /**
     * This method finds the total distance between each active sailor given as parameter to the client's destination.<br>
     * The calculation is done by summing the distance calculated by the algorithm between the sailor boat to the client plus the distance between the client to the destination.
     */
    private List<Pair<ActiveSailor, Double>> findSailorsJourneyTotalDistancesList(List<ActiveSailor> freeActiveSailorList, LocationData clientLocationData, LatLng destinationCoordinates) {
        try {
            var clientCoordinates = GeoUtils.getCoordinates(clientLocationData);

            var distanceToClientList = getDistanceToCoordinatesList(freeActiveSailorList, clientCoordinates);
            var distanceToDestinationList = getDistanceToCoordinatesList(freeActiveSailorList, destinationCoordinates);

            if (distanceToClientList.size() != distanceToDestinationList.size())
                log.warn("Distance lists sizes are different. Computation time will increase.");

            var totalDistanceList = sumListsOfDistances(distanceToClientList, distanceToDestinationList);

            log.info("Calculated the fallowing list for client coordinates: " + clientCoordinates + " and destination coordinates: " + destinationCoordinates + ":\n" + totalDistanceList);
            return totalDistanceList;
        } catch (Exception e) {
            log.error("Exception occurred while searching for sailors.");
            return null;
        }
    }

    /**
     * This method calls all the required services in order to build the response given the data calculated earlier.
     */
    private JourneyResponse buildResponseWithData(List<Pair<ActiveSailor, Double>> totalDistancesList) {
        List<SailorDetails> availableSailorsDetails = new LinkedList<>();

        for (var pair : totalDistancesList) {
            try {
                ActiveSailor sailor = pair.getFirst();
                double totalDistance = pair.getSecond();

                var sailorLocationData = sailor.getLocationData();

                var accountId = sailor.getAccountId().toString();

                String name = null;
                var accountOptional = accountsRepository.findById(sailor.getAccountId());
                if (accountOptional.isPresent()) {
                    var account = accountOptional.get();
                    name = account.getUsername();
                    if (account.getAccountDetails() != null && account.getAccountDetails().getFullName() != null)
                        name = account.getAccountDetails().getFullName();
                }

                var rating = sailor.getAverageRating();

                var costPair = geoService.estimateCost(totalDistance, sailor.getBoat());
                var estimatedCostCurrency = costPair.getFirst();
                var estimatedCost = String.valueOf(costPair.getSecond());

                var estimatedDuration = geoService.estimateDuration(totalDistance, sailor.getBoat());
                var estimatedTimeOfArrival = geoService.estimateTimeOfArrival(estimatedDuration);

                var sailorDetails = SailorDetails.builder()
                        .sailorId(accountId)
                        .sailorName(name)
                        .locationData(sailorLocationData)
                        .averageRating(rating)
                        .estimatedCost(estimatedCost)
                        .estimatedCostCurrency(estimatedCostCurrency)
                        .estimatedDuration(estimatedDuration)
                        .estimatedTimeOfArrival(estimatedTimeOfArrival)
                        .build();

                availableSailorsDetails.add(sailorDetails);
            } catch (Exception e) {
                log.error("Exception occurred while building response for activeSailor:\n" + pair.getFirst(), e);
            }
        }

        if (totalDistancesList.size() != 0 && availableSailorsDetails.size() == 0) {
            log.error("Failed to build response.");
            return null;
        }
        return new JourneyResponse(availableSailorsDetails);
    }

    public JourneyResponse requestJourney(String token, JourneyRequest request) {
        var foundAccount = accountsService.getAccountByTokenAndCredentials(token, request.getClientAccount());
        if (foundAccount == null) {
            log.warn("Token not found or token not matching the request account.");
            return null;
        }

        if (request.getClientAccount().getType() == UserType.SAILOR || foundAccount.getType() == UserType.SAILOR) {
            log.warn("Request account or account found in the database are not client accounts.");
            return null;
        }
        var currentLocationData = request.getCurrentLocationData();
        var destinationCoordinates = request.getDestinationCoordinates();

        log.info("Credentials are ok. Searching for sailors...");

        var freeActiveSailors = activeSailorsRepository.findAllFreeActiveSailors(MAX_ACTIVE_SECONDS);
        if (freeActiveSailors.isEmpty()) {
            log.info("There are no free active sailors in the last " + MAX_ACTIVE_SECONDS + " seconds.");
            return new JourneyResponse(new LinkedList<>());
        }

        var totalDistancesList = findSailorsJourneyTotalDistancesList(freeActiveSailors, currentLocationData, destinationCoordinates);
        if (totalDistancesList == null) {
            log.error("Failed to search for sailors.");
            return null;
        }

        totalDistancesList.sort(Comparator.comparingDouble(Pair::getSecond));
        totalDistancesList = totalDistancesList.subList(0, Math.min(MAX_ACTIVE_SAILORS, totalDistancesList.size()));

        var journeyResponse = buildResponseWithData(totalDistancesList);

        if (journeyResponse == null) {
            log.error("Failed to build JourneyResponse.");
            return null;
        }
        return journeyResponse;
    }
}
