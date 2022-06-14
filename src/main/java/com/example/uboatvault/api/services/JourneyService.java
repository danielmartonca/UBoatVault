package com.example.uboatvault.api.services;

import com.example.uboatvault.api.model.enums.UserType;
import com.example.uboatvault.api.model.other.LatLng;
import com.example.uboatvault.api.model.other.SailorDetails;
import com.example.uboatvault.api.model.persistence.account.Account;
import com.example.uboatvault.api.model.persistence.sailing.Stage;
import com.example.uboatvault.api.model.persistence.sailing.sailor.ActiveSailor;
import com.example.uboatvault.api.model.persistence.sailing.Journey;
import com.example.uboatvault.api.model.persistence.sailing.LocationData;
import com.example.uboatvault.api.model.requests.JourneyRequest;
import com.example.uboatvault.api.model.requests.MostRecentRidesRequest;
import com.example.uboatvault.api.model.requests.PulseRequest;
import com.example.uboatvault.api.model.requests.SailorConnectionRequest;
import com.example.uboatvault.api.model.response.JourneyResponse;
import com.example.uboatvault.api.model.response.JourneyConnectionResponse;
import com.example.uboatvault.api.repositories.AccountsRepository;
import com.example.uboatvault.api.repositories.ActiveSailorsRepository;
import com.example.uboatvault.api.repositories.JourneyRepository;
import com.example.uboatvault.api.repositories.LocationDataRepository;
import com.example.uboatvault.api.utilities.GeoUtils;
import com.example.uboatvault.api.utilities.ListUtilities;
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
    private final LocationDataRepository locationDataRepository;

    @Autowired
    public JourneyService(AccountsService accountsService, GeoService geoService, AccountsRepository accountsRepository, JourneyRepository journeyRepository, ActiveSailorsRepository activeSailorsRepository, LocationDataRepository locationDataRepository1) {
        this.accountsService = accountsService;
        this.geoService = geoService;
        this.accountsRepository = accountsRepository;
        this.journeyRepository = journeyRepository;
        this.activeSailorsRepository = activeSailorsRepository;
        this.locationDataRepository = locationDataRepository1;
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

        List<Journey> foundJourneys = journeyRepository.findAllByClient_IdAndStatus(foundAccount.getId(), Stage.FINISHED);
        if (foundJourneys == null || foundJourneys.isEmpty()) {
            log.warn("User has no completed journeys.");
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
    public boolean addFakeJourney(String clientId, String sailorId) {
        var clientAccount = accountsRepository.findById(Long.parseLong(clientId));
        var sailorAccount = accountsRepository.findById(Long.parseLong(sailorId));

        if (clientAccount.isPresent() && sailorAccount.isPresent()) {
            List<LocationData> locationDataSet = new LinkedList<>();
            var locationData = LocationData.createRandomLocationData();
            locationDataSet.add(locationData);
            Journey journey = Journey.builder().client(clientAccount.get()).sailor(
                            sailorAccount.get())
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
            locationData.setJourney(journey);
            journey.setLocationDataList(locationDataSet);
            journeyRepository.save(journey);
            log.info("Added mock data with success.");
            return true;
        }
        log.info("Client account or sailor account don't exist");
        return false;
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

    /**
     * This method returns to the client all the available sailors that will be rendered on his map after clicking find sailor
     */
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

    /**
     * This method establishes the connection between the client and the sailor after the client has chosen the sailorId sent from the request body.
     * - it searches first checks the token and account in the request. If any have problems, null is returned
     * - then it finds the free active sailor in the database active in the last MAX_ACTIVE_SECONDS seconds and has boolean lookingForClients=true.
     * If id given by client is wrong, null is returned. If the account is not found, returns message notifying user that the sailor may be busy or has gone offline.
     * - if the active sailor was found, a new journey with status ESTABLISHING_CONNECTION will be created with the given data by the client
     */
    public JourneyConnectionResponse.PossibleResponse connectToSailor(String token, SailorConnectionRequest request) {
        var clientAccount = accountsService.getAccountByTokenAndCredentials(token, request.getJourneyRequest().getClientAccount());
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

        var activeSailor = activeSailorsRepository.findFreeActiveSailorById(MAX_ACTIVE_SECONDS, sailorId);
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
     * This method updates the active sailor account found by token and credentials location data to locationData and lastUpdate to the current system time
     * in order to mark this user as currently active.
     *
     * @return true if the update was done successfully, null if the account couldn't be found and false if there was any exception during the flow
     */
    @Transactional
    public Boolean pulse(String token, PulseRequest request) {
        var account = request.getAccount();
        var locationData = request.getLocationData();
        var lookingForClients = request.isLookingForClients();
        try {

            Account foundAccount = accountsService.getAccountByTokenAndCredentials(token, account);
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

            var sailor = activeSailorsRepository.findFirstByAccountId(foundAccount.getId());
            if (sailor == null) {
                log.warn("Couldn't find active sailor account by id '" + foundAccount.getId() + "'");
                return null;
            }
            log.info("Sailor account found with the account id found earlier.");

            var oldLocationData = sailor.getLocationData();
            sailor.setLocationData(locationData);
            sailor.setLastUpdate(new Date());
            sailor.setLookingForClients(lookingForClients);
            activeSailorsRepository.save(sailor);
            log.info("Updated active sailor location data and status via pulse. ");
            if (oldLocationData != null) {
                locationDataRepository.deleteById(oldLocationData.getId());
                log.info("Deleted old location data with id: " + oldLocationData.getId());
            }

            log.info("Returning true");
            return true;
        } catch (Exception e) {
            log.error("Exception occurred during pulse workflow. Returning false", e);
            return false;
        }
    }

    /**
     * This method searches for journey objects that have status ESTABLISHING_CONNECTION for the sailor from request and returns the objects.
     * Returns null if any authentication problems occurred, a list of journeys if there are clients that chose this sailor or empty list otherwise
     */
    public List<Journey> findClients(String token, Account account) {
        try {
            var foundAccount = accountsService.getAccountByTokenAndCredentials(token, account);
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

            var sailor = activeSailorsRepository.findFirstByAccountId(foundAccount.getId());
            if (sailor == null) {
                log.warn("Couldn't find active sailor account by id '" + foundAccount.getId() + "'");
                return null;
            }
            log.info("Sailor account found with the account id found earlier.");

            if (!sailor.isLookingForClients()) {
                log.info("Setting status of sailor of lookingForClients to true.");
                sailor.setLookingForClients(true);
                activeSailorsRepository.save(sailor);
            }

            var journeys = journeyRepository.findAllBySailor_IdAndStatus(foundAccount.getId(), Stage.ESTABLISHING_CONNECTION);
            if (journeys == null || journeys.isEmpty()) {
                log.info("No new clients for the sailor.");
                return new LinkedList<>();
            }
            journeys.forEach((journey -> journey.setSailorId(sailor.getId())));
            return journeys;
        } catch (Exception e) {
            log.error("Exception occurred during findClients workflow. Returning null.", e);
            return null;
        }
    }

    public JourneyConnectionResponse.PossibleResponse selectClient(String token, Account account, Journey journey) {
        try {
            var foundAccount = accountsService.getAccountByTokenAndCredentials(token, account);
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

            var sailor = activeSailorsRepository.findFirstByAccountId(foundAccount.getId());
            if (sailor == null) {
                log.warn("Couldn't find active sailor account by id '" + foundAccount.getId() + "'");
                return JourneyConnectionResponse.PossibleResponse.ERROR;
            }
            log.info("Sailor account found with the account id found earlier.");

            if (!sailor.isLookingForClients()) {
                log.info("Setting status of sailor of lookingForClients to true.");
                sailor.setLookingForClients(true);
                activeSailorsRepository.save(sailor);
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
