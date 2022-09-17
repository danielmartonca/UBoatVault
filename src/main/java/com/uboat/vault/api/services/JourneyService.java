package com.uboat.vault.api.services;

import com.uboat.vault.api.model.enums.UBoatStatus;
import com.uboat.vault.api.model.enums.UserType;
import com.uboat.vault.api.model.http.UBoatResponse;
import com.uboat.vault.api.model.http.new_requests.RequestJourney;
import com.uboat.vault.api.model.http.new_requests.RequestPulse;
import com.uboat.vault.api.model.http.requests.JourneyRequest;
import com.uboat.vault.api.model.http.requests.SailorConnectionRequest;
import com.uboat.vault.api.model.http.response.JourneyConnectionResponse;
import com.uboat.vault.api.model.http.response.JourneyResponse;
import com.uboat.vault.api.model.other.Credentials;
import com.uboat.vault.api.model.other.LatLng;
import com.uboat.vault.api.model.other.SailorDetails;
import com.uboat.vault.api.model.persistence.account.Account;
import com.uboat.vault.api.model.persistence.sailing.Journey;
import com.uboat.vault.api.model.persistence.sailing.LocationData;
import com.uboat.vault.api.model.persistence.sailing.Stage;
import com.uboat.vault.api.model.persistence.sailing.sailor.Sailor;
import com.uboat.vault.api.repositories.AccountsRepository;
import com.uboat.vault.api.repositories.JourneyRepository;
import com.uboat.vault.api.repositories.LocationDataRepository;
import com.uboat.vault.api.repositories.SailorsRepository;
import com.uboat.vault.api.utilities.GeoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class JourneyService {
    private final Logger log = LoggerFactory.getLogger(JourneyService.class);

    @Value("${uboat.max-active-seconds}")
    private int MAX_ACTIVE_SECONDS;
    @Value("${uboat.max-active-sailors}")
    private int MAX_ACTIVE_SAILORS;

    private final EntityService entityService;
    private final GeoService geoService;
    private final JwtService jwtService;

    private final AccountsRepository accountsRepository;
    private final JourneyRepository journeyRepository;
    private final SailorsRepository sailorsRepository;
    private final LocationDataRepository locationDataRepository;

    @Autowired
    public JourneyService(EntityService entityService, GeoService geoService, JwtService jwtService, AccountsRepository accountsRepository, JourneyRepository journeyRepository, SailorsRepository sailorsRepository, LocationDataRepository locationDataRepository) {
        this.entityService = entityService;
        this.geoService = geoService;
        this.jwtService = jwtService;
        this.accountsRepository = accountsRepository;
        this.journeyRepository = journeyRepository;
        this.sailorsRepository = sailorsRepository;
        this.locationDataRepository = locationDataRepository;
    }

    @Transactional
    public boolean addFakeJourney(String clientId, String sailorId) {
        var clientAccount = accountsRepository.findById(Long.parseLong(clientId));
        var sailorAccount = accountsRepository.findById(Long.parseLong(sailorId));

        if (clientAccount.isPresent() && sailorAccount.isPresent()) {
            if (clientAccount.get().getType() != UserType.CLIENT || sailorAccount.get().getType() != UserType.SAILOR) {
                log.info("Invalid clientId or sailorId");
                return false;
            }

            List<LocationData> locationDataSet = new LinkedList<>();
            var locationData = LocationData.createRandomLocationData();
            locationDataSet.add(locationData);
            Journey journey = Journey.builder()
                    .client(clientAccount.get())
                    .sailor(sailorAccount.get())
                    .status(Stage.FINISHED)
                    .dateBooking(new Date())
                    .dateArrival(new Date())
                    .sourceLatitude(37.430857)
                    .sourceLongitude(-122.091288)
                    .sourceAddress("CWJ5+ 79 Mountain View,CA,USA")
                    .destinationLatitude(37.434588)
                    .destinationLongitude(-122.093799)
                    .destinationAddress("CWM4+RFP Mountain View,CA,USA")
                    .payment("10 EUR")
                    .duration("10 minutes")
                    .build();
            journey.setLocationDataList(locationDataSet);
            journeyRepository.save(journey);
            log.info("Added mock data with success.");
            return true;
        }
        log.info("Client account or sailor account don't exist");
        return false;
    }

    public UBoatResponse getMostRecentRides(String authorizationHeader, Integer ridesRequested) {
        try {
            //cant be null because the operation is already done in the filter before
            var jwtData = jwtService.extractUsernameAndPhoneNumberFromHeader(authorizationHeader);
            var account = entityService.findAccountByUsername(jwtData.username());

            List<Journey> foundJourneys = journeyRepository.findAllByClient_IdAndStatus(account.getId(), Stage.FINISHED);
            if (foundJourneys == null || foundJourneys.isEmpty()) {
                log.info("User has no completed journeys.");
                return new UBoatResponse(UBoatStatus.MOST_RECENT_RIDES_RETRIEVED, new LinkedList<>());
            }
            log.info("Found journeys for the user.");


            List<RequestJourney> journeys = new LinkedList<>();
            for (var journey : foundJourneys) {
                if (ridesRequested == 0) break;
                journeys.add(new RequestJourney(journey));
                ridesRequested--;
            }

            return new UBoatResponse(UBoatStatus.MOST_RECENT_RIDES_RETRIEVED, journeys);
        } catch (Exception e) {
            log.error("An exception occurred during getMostRecentRides workflow.", e);
            return new UBoatResponse(UBoatStatus.VAULT_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * This method returns a list of Pairs < ActiveSailor, distance > where each pair represents
     * the distance from the ActiveSailor's coordinates to the destinationCoordinates given as parameter
     */
    private List<Pair<Sailor, Double>> getDistanceToCoordinatesList(List<Sailor> sailors, LatLng destinationCoordinates) {
        List<Pair<Sailor, Double>> activeSailorsDistanceList = new LinkedList<>();
        for (var activeSailor : sailors) {
            try {
                LatLng sailorCoordinates = GeoUtils.getCoordinates(activeSailor.getCurrentLocation());

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
    private List<Pair<Sailor, Double>> sumListsOfDistances(List<Pair<Sailor, Double>> list1, List<Pair<Sailor, Double>> list2) {
        List<Pair<Sailor, Double>> totalDistanceList = new LinkedList<>();

        for (int i = 0; i < Math.min(list1.size(), list2.size()); i++) {
            var pair1 = list1.get(i);
            var pair2 = list2.get(i);

            if (pair1.getFirst().getAccountId().equals(pair2.getFirst().getAccountId())) {
                totalDistanceList.add(Pair.of(pair1.getFirst(), pair1.getSecond() + pair2.getSecond()));
            } else {
                Pair<Sailor, Double> pair;
                List<Pair<Sailor, Double>> searchList;
                if (pair1.getFirst().getAccountId() >= pair2.getFirst().getAccountId()) {
                    pair = pair1;
                    searchList = list2;
                } else {
                    pair = pair2;
                    searchList = list1;
                }
                Long sailorAccountId = pair.getFirst().getAccountId();

                searchList.stream()
                        .filter(activeSailorPair -> sailorAccountId.equals(activeSailorPair.getFirst().getAccountId()))
                        .findFirst()
                        .ifPresent(activeSailorDoublePair -> totalDistanceList.add(Pair.of(pair.getFirst(), pair.getSecond() + activeSailorDoublePair.getSecond())));
            }
        }

        return totalDistanceList;
    }

    /**
     * This method finds the total distance between each active sailor given as parameter to the client's destination.<br>
     * The calculation is done by summing the distance calculated by the algorithm between the sailor boat to the client plus the distance between the client to the destination.
     */
    private List<Pair<Sailor, Double>> findSailorsJourneyTotalDistancesList(List<Sailor> freeSailorList, LocationData clientLocationData, LatLng destinationCoordinates) {
        try {
            var clientCoordinates = GeoUtils.getCoordinates(clientLocationData);

            var distanceToClientList = getDistanceToCoordinatesList(freeSailorList, clientCoordinates);
            var distanceToDestinationList = getDistanceToCoordinatesList(freeSailorList, destinationCoordinates);

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
    private JourneyResponse buildResponseWithData(List<Pair<Sailor, Double>> totalDistancesList) {
        List<SailorDetails> availableSailorsDetails = new LinkedList<>();

        for (var pair : totalDistancesList) {
            try {
                Sailor sailor = pair.getFirst();
                double totalDistance = pair.getSecond();

                var sailorLocationData = sailor.getCurrentLocation();

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

    /**
     * This method returns to the client all the available sailors that will be rendered on his map after clicking find sailor
     */
    public JourneyResponse requestJourney(JourneyRequest request) {
        var foundAccount = entityService.findAccountByCredentials(Credentials.fromAccount(request.getClientAccount()));
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

        var freeActiveSailors = sailorsRepository.findAllFreeActiveSailors(MAX_ACTIVE_SECONDS);
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

    /**
     * This method establishes the connection between the client and the sailor after the client has chosen the sailorId sent from the request body.
     * - it searches first checks the token and account in the request. If any have problems, null is returned
     * - then it finds the free active sailor in the database active in the last MAX_ACTIVE_SECONDS seconds and has boolean lookingForClients=true.
     * If id given by client is wrong, null is returned. If the account is not found, returns message notifying user that the sailor may be busy or has gone offline.
     * - if the active sailor was found, a new journey with status ESTABLISHING_CONNECTION will be created with the given data by the client
     */
    public JourneyConnectionResponse.PossibleResponse connectToSailor(SailorConnectionRequest request) {
        var clientAccount = entityService.findAccountByCredentials(Credentials.fromAccount(request.getJourneyRequest().getClientAccount()));
        if (clientAccount == null)
            return JourneyConnectionResponse.PossibleResponse.ERROR;

        if (request.getJourneyRequest().getClientAccount().getType() == UserType.SAILOR || clientAccount.getType() == UserType.SAILOR) {
            log.warn("Request account or account found in the database are not client accounts.");
            return null;
        }
        log.info("Credentials are ok. Connecting to the sailor...");

        long sailorId;
        try {
            sailorId = Long.parseLong(request.getSailorId());
        } catch (Exception e) {
            log.warn("Failed to parse sailor id from request. SailorId: " + request.getSailorId(), e);
            return JourneyConnectionResponse.PossibleResponse.ERROR;
        }

        var activeSailor = sailorsRepository.findFreeActiveSailorById(MAX_ACTIVE_SAILORS, sailorId);
        if (activeSailor == null) {
            log.warn("Couldn't find the active sailor. Either the sailor is busy,not active or the user request is wrong.");
            return JourneyConnectionResponse.PossibleResponse.SAILOR_NOT_FOUND;
        }

        var activeSailorAccountOptional = accountsRepository.findById(activeSailor.getAccountId());
        if (activeSailorAccountOptional.isEmpty()) {
            log.warn("Found active sailor but couldn't find account.");
            return JourneyConnectionResponse.PossibleResponse.SERVER_ERROR;
        }

        var sailorAccount = activeSailorAccountOptional.get();

        var currentLocationData = request.getJourneyRequest().getCurrentLocationData();
        var destinationCoordinates = request.getJourneyRequest().getDestinationCoordinates();

        try {
            Double.parseDouble(currentLocationData.getLatitude());
            Double.parseDouble(currentLocationData.getLongitude());
        } catch (Exception e) {
            log.error("Failed to parse current location data coordinates.");
            return JourneyConnectionResponse.PossibleResponse.ERROR;
        }

        var newJourney = Journey.builder()
                .status(Stage.ESTABLISHING_CONNECTION)
                .client(clientAccount)
                .sailor(sailorAccount)
                .dateBooking(new Date())
                .sourceLatitude(Double.parseDouble(currentLocationData.getLatitude()))
                .sourceLongitude(Double.parseDouble(currentLocationData.getLongitude()))
                .sourceAddress(request.getSourceAddress())
                .destinationLatitude(destinationCoordinates.getLatitude())
                .destinationLongitude(destinationCoordinates.getLongitude())
                .destinationAddress(request.getDestinationAddress())
                .build();

        journeyRepository.save(newJourney);

        return JourneyConnectionResponse.PossibleResponse.CONNECT_TO_SAILOR_SUCCESS;
    }

    /**
     * This method updates the sailor account locationData and lastUpdate to the current system time in order to mark this sailor as active
     */
    @Transactional
    public UBoatResponse pulse(String authorizationHeader, RequestPulse pulseRequest) {
        try {
            //cant be null because the operation is already done in the filter before
            var jwtData = jwtService.extractUsernameAndPhoneNumberFromHeader(authorizationHeader);
            var sailor = entityService.findSailorByJwt(jwtData);

            var oldLocationData = sailor.getCurrentLocation();
            sailor.setCurrentLocation(pulseRequest.getLocationData());
            sailor.setLookingForClients(pulseRequest.isLookingForClients());
            sailor.setLastUpdate(new Date());

            sailorsRepository.save(sailor);
            log.info("Updated sailor location data and status via pulse.");

            if (oldLocationData != null) {
                locationDataRepository.deleteById(oldLocationData.getId());
                log.debug("Deleted old location data.");
            }
            return new UBoatResponse(UBoatStatus.PULSE_SUCCESSFUL, true);
        } catch (Exception e) {
            log.error("An exception occurred during pulse workflow.", e);
            return new UBoatResponse(UBoatStatus.VAULT_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * This method searches for journey objects that have status ESTABLISHING_CONNECTION for the sailor from request and returns the objects.
     * Returns null if any authentication problems occurred, a list of journeys if there are clients that chose this sailor or empty list otherwise
     */
    public List<Journey> findClients(Account account) {
        try {
            var foundAccount = entityService.findAccountByCredentials(Credentials.fromAccount(account));
            if (foundAccount == null) {
                log.info("Request account or token are invalid.");
                return null;
            }
            log.info("Token and credentials match.");

            if (foundAccount.getType() == UserType.CLIENT) {
                log.warn("Account and token match but account is not a sailor account.");
                return null;
            }
            log.info("Account is a sailor account.");

            var sailor = sailorsRepository.findFirstByAccountId(foundAccount.getId());
            if (sailor == null) {
                log.warn("Couldn't find active sailor account by id '" + foundAccount.getId() + "'");
                return null;
            }
            log.info("Sailor account found with the account id found earlier.");

            if (!sailor.isLookingForClients()) {
                log.info("Setting status of sailor of lookingForClients to true.");
                sailor.setLookingForClients(true);
                sailorsRepository.save(sailor);
            }

            var journeys = journeyRepository.findAllBySailor_IdAndStatus(foundAccount.getId(), Stage.ESTABLISHING_CONNECTION);
            if (journeys == null || journeys.isEmpty()) {
                log.info("No new clients for the sailor.");
                return new LinkedList<>();
            }
            return journeys;
        } catch (Exception e) {
            log.error("Exception occurred during findClients workflow. Returning null.", e);
            return null;
        }
    }

    public JourneyConnectionResponse.PossibleResponse selectClient(Account account, Journey journey) {
        try {
            var foundAccount = entityService.findAccountByCredentials(Credentials.fromAccount(account));
            if (foundAccount == null) {
                log.info("Request account or token are invalid.");
                return JourneyConnectionResponse.PossibleResponse.ERROR;
            }
            log.info("Token and credentials match.");

            if (foundAccount.getType() == UserType.CLIENT) {
                log.warn("Account and token match but account is not a sailor account.");
                return JourneyConnectionResponse.PossibleResponse.ERROR;
            }
            log.info("Account is a sailor account.");

            var sailor = sailorsRepository.findFirstByAccountId(foundAccount.getId());
            if (sailor == null) {
                log.warn("Couldn't find active sailor account by id '" + foundAccount.getId() + "'");
                return JourneyConnectionResponse.PossibleResponse.ERROR;
            }
            log.info("Sailor account found with the account id found earlier.");

            if (!sailor.isLookingForClients()) {
                log.info("Setting status of sailor of lookingForClients to true.");
                sailor.setLookingForClients(true);
                sailorsRepository.save(sailor);
            }

            var latitude = journey.getDestinationLatitude();
            var longitude = journey.getDestinationLongitude();

            var foundJourney = journeyRepository.findBySailor_IdAndStatusAndDestinationLatitudeAndDestinationLongitude(foundAccount.getId(), Stage.ESTABLISHING_CONNECTION, latitude, longitude);
            if (foundJourney == null) {
                log.warn("Failed to find journey in request");
                return JourneyConnectionResponse.PossibleResponse.JOURNEY_NOT_FOUND;
            }
            log.info("Found journey in the request with status ESTABLISHING_CONNECTION. Journey id:" + foundJourney.getId());
            var otherJourneys = journeyRepository.findAllBySailor_IdAndStatus(foundAccount.getId(), Stage.ESTABLISHING_CONNECTION);
            if (otherJourneys != null && !otherJourneys.isEmpty())
                for (var otherJourney : otherJourneys)
                    if (!Objects.equals(otherJourney.getClient().getId(), foundJourney.getClient().getId())) {
                        otherJourney.setStatus(Stage.CANCELED);
                        journeyRepository.save(otherJourney);
                    }

            foundJourney.setStatus(Stage.SAILOR_ACCEPTED);
            journeyRepository.save(foundJourney);
            log.info("Set status of journey from ESTABLISHING_CONNECTION to SAILOR_ACCEPTED. Journey id:" + foundJourney.getId());
            return JourneyConnectionResponse.PossibleResponse.SELECT_CLIENT_SUCCESS;
        } catch (Exception e) {
            log.error("Exception occurred during selectClient workflow. Returning null.", e);
            return JourneyConnectionResponse.PossibleResponse.SERVER_ERROR;
        }
    }
}
