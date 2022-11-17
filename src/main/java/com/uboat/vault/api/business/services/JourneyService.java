package com.uboat.vault.api.business.services;

import com.uboat.vault.api.model.domain.sailing.Journey;
import com.uboat.vault.api.model.domain.sailing.LocationData;
import com.uboat.vault.api.model.domain.sailing.sailor.Sailor;
import com.uboat.vault.api.model.dto.*;
import com.uboat.vault.api.model.enums.Stage;
import com.uboat.vault.api.model.enums.UBoatStatus;
import com.uboat.vault.api.model.enums.UserType;
import com.uboat.vault.api.model.other.LatLng;
import com.uboat.vault.api.persistence.repostiories.AccountsRepository;
import com.uboat.vault.api.persistence.repostiories.JourneyRepository;
import com.uboat.vault.api.persistence.repostiories.LocationDataRepository;
import com.uboat.vault.api.persistence.repostiories.SailorsRepository;
import com.uboat.vault.api.utilities.DateUtils;
import com.uboat.vault.api.utilities.GeoUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class JourneyService {
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
                    .clientAccount(clientAccount.get())
                    .sailorAccount(sailorAccount.get())
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


    public UBoatDTO getMostRecentRides(String authorizationHeader, Integer ridesRequested) {
        try {
            //cant be null because the operation is already done in the filter before
            var jwtData = jwtService.extractUsernameAndPhoneNumberFromHeader(authorizationHeader);
            var account = entityService.findAccountByJwtData(jwtData);

            List<Journey> foundJourneys = journeyRepository.findAllByClientAccount_IdAndStatus(account.getId(), Stage.FINISHED);
            if (foundJourneys == null || foundJourneys.isEmpty()) {
                log.info("User has no completed journeys.");
                return new UBoatDTO(UBoatStatus.MOST_RECENT_RIDES_RETRIEVED, new LinkedList<>());
            }
            log.info("Found journeys for the user.");


            List<JourneyDTO> journeys = new LinkedList<>();
            for (var journey : foundJourneys) {
                if (ridesRequested == 0) break;
                journeys.add(new JourneyDTO(journey));
                ridesRequested--;
            }

            return new UBoatDTO(UBoatStatus.MOST_RECENT_RIDES_RETRIEVED, journeys);
        } catch (Exception e) {
            log.error("An exception occurred during getMostRecentRides workflow.", e);
            return new UBoatDTO(UBoatStatus.VAULT_INTERNAL_SERVER_ERROR);
        }
    }

    //request journey

    /**
     * This method returns a list of Pairs < ActiveSailor, distance > where each pair represents
     * the distance from the ActiveSailor's coordinates to the destinationCoordinates given as parameter
     */
    private List<Pair<Sailor, Double>> getDistanceToCoordinatesList(List<Sailor> sailors, LatLng destinationCoordinates) {
        List<Pair<Sailor, Double>> sailorsDistancesList = new LinkedList<>();
        for (var sailor : sailors) {
            try {
                LatLng sailorCoordinates = GeoUtils.getCoordinates(sailor.getCurrentLocation());

                double distanceToDestination = geoService.calculateDistanceBetweenCoordinates(sailorCoordinates, destinationCoordinates);

                var pair = Pair.of(sailor, distanceToDestination);
                sailorsDistancesList.add(pair);
            } catch (Exception e) {
                log.error("Exception during distance calculation workflow for active sailor:\n" + sailor);
            }
        }
        sailorsDistancesList.sort(Comparator.comparingLong(pair -> pair.getFirst().getAccountId()));
        return sailorsDistancesList;
    }

    /**
     * This method takes two lists of pairs calculated with {@link #getDistanceToCoordinatesList(List, LatLng)}
     * and sums pairs that match Sailor objects by accountId.
     * <br>If there are pairs in any of the two lists where the ActiveSailor does not have a correspondence in the other list, they will be completely ignored by the algorithm.
     * <p><br>
     * This method returns a list of pairs sorted by Sailor id!
     * TODO - merge lists using merge sort technique
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
    private List<Pair<Sailor, Double>> findSailorsJourneyTotalDistancesList(List<Sailor> freeSailorList, LatLng clientCoordinates, LatLng destinationCoordinates) {
        try {
            var distanceToClientList = getDistanceToCoordinatesList(freeSailorList, clientCoordinates);
            var distanceToDestinationList = getDistanceToCoordinatesList(freeSailorList, destinationCoordinates);

            var totalDistanceList = sumListsOfDistances(distanceToClientList, distanceToDestinationList);

            log.debug("Calculated the fallowing list for client coordinates: " + clientCoordinates + " and destination coordinates: " + destinationCoordinates + ":\n" + totalDistanceList);
            log.info("Calculated the sum of distances list.");
            return totalDistanceList;
        } catch (Exception e) {
            log.error("Exception occurred while searching for sailors.", e);
            throw e;
        }
    }

    /**
     * This method calls all the required services in order to build the response given the data calculated earlier.
     */
    private NewJourneyDetailsDTO buildNewJourneyDetails(Pair<Sailor, Double> sailorDoublePair) {
        try {
            var sailor = sailorDoublePair.getFirst();
            var distance = sailorDoublePair.getSecond();

            //retrieve sailor name from the account details if existing or account username otherwise
            var accountOptional = accountsRepository.findById(sailor.getAccountId());
            if (accountOptional.isEmpty())
                throw new RuntimeException("Warning: sailor has account id which does not belong to any account");

            var account = accountOptional.get();
            var sailorName = account.getUsername();
            if (account.getAccountDetails().getFullName() != null)
                sailorName = account.getAccountDetails().getFullName();

            var costPair = geoService.estimateCost(distance, sailor.getBoat());
            var estimatedDuration = geoService.estimateDuration(distance, sailor.getBoat());

            return NewJourneyDetailsDTO.builder()
                    .sailorId(String.valueOf(sailor.getId()))
                    .sailorName(sailorName)
                    .sailorCurrentLocation(new LocationDataDTO(sailor.getCurrentLocation()))
                    .distance(distance)
                    .estimatedCost(String.valueOf(costPair.getSecond()))
                    .estimatedCostCurrency(costPair.getFirst())
                    .estimatedDuration(estimatedDuration)
                    .estimatedTimeOfArrival(geoService.estimateTimeOfArrival(estimatedDuration))
                    .build();
        } catch (RuntimeException e) {
            log.error("Exception while building response");
            return null;
        }
    }

    /**
     * This method returns to the client all the available sailors that will be rendered on his map after clicking find sailor
     */
    @Transactional
    public UBoatDTO requestJourney(NewJourneyDTO request) {
        try {
            log.info("Searching for sailors...");

            //queried all active sailors
            var activeSailors = sailorsRepository.findAllByLookingForClients(true);

            //removed active sailors who are not active in the last  MAX_ACTIVE_SECONDS seconds and set their status as not looking for clients
            var freeSailors = activeSailors.stream()
                    .filter(sailor -> {
                        if (DateUtils.getSecondsPassed(sailor.getLastUpdate()) >= MAX_ACTIVE_SECONDS) {
                            sailor.setLookingForClients(false);
                            sailorsRepository.save(sailor);
                            return false;
                        }
                        return true;
                    })
                    .toList();

            if (freeSailors.isEmpty()) {
                log.info("There are no free active sailors in the last " + MAX_ACTIVE_SECONDS + " seconds.");
                return new UBoatDTO(UBoatStatus.NO_FREE_SAILORS_FOUND);
            }
            log.info("{} free sailors were found. ", freeSailors.size());

            var totalDistancesList = findSailorsJourneyTotalDistancesList(freeSailors, request.getCurrentCoordinates(), request.getDestinationCoordinates());

            //sort the list by the shortest distance
            totalDistancesList.sort(Comparator.comparingDouble(Pair::getSecond));
            //only return the last MAX_ACTIVE_SAILORS
            totalDistancesList = totalDistancesList.subList(0, Math.min(MAX_ACTIVE_SAILORS, totalDistancesList.size()));

            //build data that will be sent to the user
            var responseList = totalDistancesList.stream()
                    .map(this::buildNewJourneyDetails)
                    .filter(Objects::nonNull)
                    .toList();
            return new UBoatDTO(UBoatStatus.FREE_SAILORS_FOUND, responseList);
        } catch (Exception e) {
            log.error("An exception occurred during requestJourney workflow.", e);
            return new UBoatDTO(UBoatStatus.VAULT_INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * This method updates the sailor account locationData and lastUpdate to the current system time in order to mark this sailor as active
     */
    @Transactional
    public UBoatDTO pulse(String authorizationHeader, PulseDTO pulseRequest) {
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
            return new UBoatDTO(UBoatStatus.PULSE_SUCCESSFUL, true);
        } catch (Exception e) {
            log.error("An exception occurred during pulse workflow.", e);
            return new UBoatDTO(UBoatStatus.VAULT_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * This method searches for journey objects that have status CLIENT_ACCEPTED for the sailor from request and returns the journeys.
     */
    @Transactional
    public UBoatDTO findClients(String authorizationHeader) {
        try {
            //cant be null because the operation is already done in the filter before
            var jwtData = jwtService.extractUsernameAndPhoneNumberFromHeader(authorizationHeader);
            var sailor = entityService.findSailorByJwt(jwtData);

            if (!sailor.isLookingForClients()) {
                log.info("Setting status of sailor of lookingForClients to true.");
                sailor.setLookingForClients(true);
                sailorsRepository.save(sailor);
            }

            var journeys = journeyRepository.findAllByStatusAndSailorAccount_Id(Stage.CLIENT_ACCEPTED, sailor.getAccountId());
            if (journeys == null || journeys.isEmpty())
                return new UBoatDTO(UBoatStatus.NO_CLIENTS_FOUND);

            var responseJourneys = journeys.stream().map(JourneyDTO::new).toList();
            return new UBoatDTO(UBoatStatus.CLIENTS_FOUND, responseJourneys);
        } catch (Exception e) {
            log.error("Exception occurred during findClients workflow. Returning null.", e);
            return null;
        }
    }

    /**
     * This method queries all journeys that have status CLIENT_ACCEPTED for the sailor extracted from the JWT then searches for a match of RequestJourney
     */
    @Transactional
    public UBoatDTO selectClient(String authorizationHeader, JourneyDTO journeyDTO) {
        try {
            //cant be null because the operation is already done in the filter before
            var jwtData = jwtService.extractUsernameAndPhoneNumberFromHeader(authorizationHeader);
            var sailor = entityService.findSailorByJwt(jwtData);

            if (!sailor.isLookingForClients()) {
                log.info("Setting status of sailor of lookingForClients to true.");
                sailor.setLookingForClients(true);
                sailorsRepository.save(sailor);
            }

            var journey = journeyRepository.findNewJourneyOfSailorMatchingSourceAndDestination(
                    Stage.CLIENT_ACCEPTED,
                    sailor.getAccountId(),
                    journeyDTO.getSourceLatitude(), journeyDTO.getSourceLongitude(),
                    journeyDTO.getDestinationLatitude(), journeyDTO.getDestinationLongitude());

            if (journey == null)
                return new UBoatDTO(UBoatStatus.JOURNEY_NOT_FOUND, false);

            log.info("Found journey from the request with status CLIENT_ACCEPTED. Journey id: {}", journey.getId());

            //dismiss any other journey for the current sailor - set all the other CLIENT_ACCEPTED journeys for this sailor to SAILOR_CANCELED
            var otherJourneys = journeyRepository.findAllByStatusAndSailorAccount_Id(Stage.CLIENT_ACCEPTED, sailor.getAccountId());
            if (otherJourneys != null && otherJourneys.size() > 1) {
                otherJourneys.removeIf(j -> j.getId().equals(journey.getId()));
                for (var otherJourney : otherJourneys) {
                    otherJourney.setStatus(Stage.SAILOR_CANCELED);
                    journeyRepository.save(otherJourney);
                    log.info("Canceled journey by id {}", otherJourney.getId());
                }
            }

            journey.setStatus(Stage.SAILOR_ACCEPTED);
            journeyRepository.save(journey);
            log.info("Set status of journey from CLIENT_ACCEPTED to SAILOR_ACCEPTED. Journey id: {}", journey.getId());
            return new UBoatDTO(UBoatStatus.JOURNEY_SELECTED, true);
        } catch (Exception e) {
            log.error("Exception occurred during selectClient workflow.", e);
            return new UBoatDTO(UBoatStatus.VAULT_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * This method establishes the connection between the client and the sailor after the client has chosen the sailorId sent from the request body.
     * - it finds the free sailor in the database active in the last MAX_ACTIVE_SECONDS seconds and has lookingForClients = true.
     * If the account is not found, returns message notifying user that the sailor may be busy or has gone offline.
     * - if the active sailor was found, a new journey with status CLIENT_ACCEPTED will be created with the given data by the client
     */
    @Transactional
    public UBoatDTO chooseJourney(String authorizationHeader, NewJourneyDTO request, Long sailorId) {
        try {
            //cant be null because the operation is already done in the filter before
            var jwtData = jwtService.extractUsernameAndPhoneNumberFromHeader(authorizationHeader);
            var clientAccount = entityService.findAccountByJwtData(jwtData);

            var sailor = sailorsRepository.findSailorByIdAndLookingForClientsIsTrue(sailorId);
            //if the sailor could not be found, or he has not updated his status for more than MAX_ACTIVE_SECONDS
            if (sailor == null || DateUtils.getSecondsPassed(sailor.getLastUpdate()) >= MAX_ACTIVE_SECONDS) {
                log.warn("Couldn't find the sailor. Either the sailor is busy, not active or the client request is wrong.");
                return new UBoatDTO(UBoatStatus.SAILOR_NOT_FOUND);
            }

            var sailorAccountOptional = accountsRepository.findById(sailor.getAccountId());
            if (sailorAccountOptional.isEmpty())
                throw new RuntimeException("Warning: sailor has account id which does not belong to any account");

            //cancel old journeys
            var journeys = journeyRepository.findAllByClientAccount_IdAndStatus(clientAccount.getId(), Stage.CLIENT_ACCEPTED);
            for (var oldJourney : journeys) {
                log.info("Journey with ID {} was canceled by the user.", oldJourney.getId());
                oldJourney.setStatus(Stage.CLIENT_CANCELED);
            }

            var sourceCoordinates = request.getCurrentCoordinates();
            var destinationCoordinates = request.getDestinationCoordinates();

            var journey = Journey.builder()
                    .status(Stage.CLIENT_ACCEPTED)
                    .clientAccount(clientAccount)
                    .sailorAccount(sailorAccountOptional.get())
                    .dateBooking(new Date())
                    .sourceLatitude(sourceCoordinates.getLatitude())
                    .sourceLongitude(sourceCoordinates.getLongitude())
                    .sourceAddress(request.getCurrentAddress())
                    .destinationLatitude(destinationCoordinates.getLatitude())
                    .destinationLongitude(destinationCoordinates.getLongitude())
                    .destinationAddress(request.getDestinationAddress())
                    .build();

            journeyRepository.save(journey);

            return new UBoatDTO(UBoatStatus.NEW_JOURNEY_CREATED, new JourneyDTO(journey));
        } catch (Exception e) {
            log.error("Exception occurred during selectClient workflow.", e);
            return new UBoatDTO(UBoatStatus.VAULT_INTERNAL_SERVER_ERROR);
        }
    }
}
