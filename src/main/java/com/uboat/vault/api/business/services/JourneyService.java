package com.uboat.vault.api.business.services;

import com.uboat.vault.api.model.domain.account.account.Account;
import com.uboat.vault.api.model.domain.account.account.CreditCard;
import com.uboat.vault.api.model.domain.account.sailor.Sailor;
import com.uboat.vault.api.model.domain.sailing.*;
import com.uboat.vault.api.model.dto.*;
import com.uboat.vault.api.model.enums.JourneyState;
import com.uboat.vault.api.model.enums.PaymentType;
import com.uboat.vault.api.model.enums.UBoatStatus;
import com.uboat.vault.api.model.enums.UserType;
import com.uboat.vault.api.model.exceptions.NoRouteFoundException;
import com.uboat.vault.api.model.exceptions.UBoatJwtException;
import com.uboat.vault.api.persistence.repostiories.JourneyRepository;
import com.uboat.vault.api.persistence.repostiories.JourneysErrorRepository;
import com.uboat.vault.api.persistence.repostiories.LocationDataRepository;
import com.uboat.vault.api.persistence.repostiories.SailorsRepository;
import com.uboat.vault.api.utilities.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class JourneyService {
    @Value("${uboat.max-active-seconds}")
    private int MAX_ACTIVE_SECONDS;
    @Value("${uboat.max-active-sailors}")
    private int MAX_ACTIVE_SAILORS;

    @Value("${uboat.max-accepted-distance}")
    private int MAX_ACCEPTED_DISTANCE;

    @Value("${uboat.journey.inactivityTimeoutSeconds}")
    private int inactivityTimeoutSeconds;

    @Value("${uboat.journey.metersDistanceForCloseConsideration}")
    private int metersDistanceForCloseConsideration;

    @Value("${uboat.journey.recordAllLocationData}")
    private Boolean recordAllLocationData = false;

    private final EntityService entityService;
    private final GeoService geoService;
    private final JwtService jwtService;

    private final JourneyRepository journeyRepository;
    private final SailorsRepository sailorsRepository;
    private final LocationDataRepository locationDataRepository;

    private final JourneysErrorRepository errorRepository;

    @Transactional
    public void setJourneyInError(Journey journey, @NotNull @NotEmpty String reason) {
        journey.setState(JourneyState.IN_ERROR);
        errorRepository.save(JourneyError.builder().journey(journey).dateRecorded(new Date()).reason(reason).build());
        journeyRepository.save(journey);
    }

    @Transactional
    public UBoatDTO getOngoingJourney(String authorizationHeader) {
        try {
            //cant be null because the operation is already done in the filter before
            var jwtData = jwtService.extractUsernameAndPhoneNumberFromHeader(authorizationHeader);
            var account = entityService.findAccountByJwtData(jwtData);

            var states = Set.of(JourneyState.VERIFYING_PAYMENT, JourneyState.SAILING_TO_CLIENT, JourneyState.SAILING_TO_DESTINATION);
            Optional<Journey> journeyOptional;

            if (account.getType() == UserType.CLIENT)
                journeyOptional = journeyRepository.findClientJourneyMatchingAccountAndState(account.getId(), states);
            else journeyOptional = journeyRepository.findSailorJourneyMatchingAccountAndState(account.getId(), states);

            if (journeyOptional.isEmpty()) return new UBoatDTO(UBoatStatus.ONGOING_JOURNEY_NOT_FOUND, null);

            var journey = journeyOptional.get();
            var dto = account.getType() == UserType.CLIENT ? JourneyDTO.buildDTOForClients(journey) : JourneyDTO.buildDTOForSailors(journey);

            return new UBoatDTO(UBoatStatus.ONGOING_JOURNEY_RETRIEVED, dto);
        } catch (Exception e) {
            log.error("An exception occurred during getOngoingJourney workflow.", e);
            return new UBoatDTO(UBoatStatus.VAULT_INTERNAL_SERVER_ERROR);
        }
    }

    private boolean isNotNearCoordinates(LatLng currentCoordinates, LatLng coordinates) {
        return (geoService.calculateDistanceBetweenCoordinates(currentCoordinates, coordinates) >= metersDistanceForCloseConsideration);
    }

    private void updateJourneyStateBasedOnPosition(Journey journey, LocationDataDTO dto, JourneyLocationInfo lastKnownLocationInfo) {
        var coordinates = new LatLng(dto);
        switch (journey.getState()) {
            case SAILING_TO_CLIENT -> {
                //has to be near the sailor
                if (isNotNearCoordinates(coordinates, lastKnownLocationInfo.getLocation().getCoordinates()))
                    break;
                log.info("Sailor has reached the client, updating journey state to {}.", JourneyState.SAILING_TO_DESTINATION);
                journey.setState(JourneyState.SAILING_TO_DESTINATION);
                journeyRepository.save(journey);
            }
            case SAILING_TO_DESTINATION -> {
                //has to be near the sailor and near the destination
                if (isNotNearCoordinates(coordinates, lastKnownLocationInfo.getLocation().getCoordinates()) || isNotNearCoordinates(coordinates, journey.getRoute().getDestinationLocation().getCoordinates()))
                    break;
                log.info("The client and the sailor have reached the destination, updating journey state to {}.", JourneyState.VERIFYING_PAYMENT);
                journey.setState(JourneyState.VERIFYING_PAYMENT);
                journeyRepository.save(journey);
            }
        }
    }

    private boolean isOtherEntityInactive(JourneyLocationInfo lastKnownLocationInfo) {
        return DateUtils.getSecondsPassed(lastKnownLocationInfo.getTimestamp()) >= inactivityTimeoutSeconds;
    }

    @Transactional
    void recordLocation(UserType userType, Journey journey, LocationDataDTO dto) {
        var locationData = new LocationData(dto);
        var location = new Location(dto);
        if (recordAllLocationData) {
            journey.getRecordedLocationInfos().add(JourneyLocationInfo.builder().recorder(userType).journeyState(journey.getState()).location(location).locationData(locationData).timestamp(new Date()).journey(journey).build());
        } else {
            var locationInfo = journey.getLastKnownLocation(userType);
            if (locationInfo == null) {
                journey.getRecordedLocationInfos().add(JourneyLocationInfo.builder().recorder(userType).journeyState(journey.getState()).location(location).locationData(locationData).timestamp(new Date()).journey(journey).build());
            } else {
                locationInfo.setLocation(location);
                locationInfo.setLocationData(locationData);
            }
        }
        journeyRepository.save(journey);
    }

    private void recordLocation(Account account, Journey journey, LocationDataDTO dto) {
        recordLocation(account.getType(), journey, dto);
    }

    @Transactional
    public UBoatDTO sail(String authorizationHeader, LocationDataDTO dto) {
        try {
            //cant be null because the operation is already done in the filter before
            var jwtData = jwtService.extractUsernameAndPhoneNumberFromHeader(authorizationHeader);
            var account = entityService.findAccountByJwtData(jwtData);
            log.info("Sail API called by the {}.", account.getType());

            var states = Set.of(JourneyState.SAILING_TO_CLIENT, JourneyState.SAILING_TO_DESTINATION, JourneyState.VERIFYING_PAYMENT);
            var journeyOptional = account.getType() == UserType.CLIENT ? journeyRepository.findClientJourneyMatchingAccountAndState(account.getId(), states) : journeyRepository.findSailorJourneyMatchingAccountAndState(account.getId(), states);

            if (journeyOptional.isEmpty()) {
                log.warn("Sail API called but there is no ongoing journey for this entity.");
                return new UBoatDTO(UBoatStatus.NOT_SAILING, null);
            }

            var journey = journeyOptional.get();
            var lastKnownLocationInfo = journey.getLastKnownLocation(account.getType() == UserType.CLIENT ? UserType.SAILOR : UserType.CLIENT);

            try {
                if (lastKnownLocationInfo == null) {
                    log.info("First sail call recorded.");
                    return new UBoatDTO(UBoatStatus.SAIL_RECORDED, new SailDTO(journey, account.getType() == UserType.CLIENT ? journey.getRoute().getSailorLocation() : journey.getRoute().getClientLocation()));
                }

                //if the other entity is considered inactive, cancel the journey
                if (isOtherEntityInactive(lastKnownLocationInfo)) {
                    log.warn("Journey set in error due to no activity being detected from the other entity in the last {} seconds. Seconds passed: {} . Last recorded timestamp: {}.", inactivityTimeoutSeconds, DateUtils.getSecondsPassed(lastKnownLocationInfo.getTimestamp()), lastKnownLocationInfo.getTimestamp());
                    setJourneyInError(journey, "The other entity failed to record any activity in the last [inactivityTimeoutSeconds] seconds.");
                    return new UBoatDTO(UBoatStatus.LOST_CONNECTION, null);
                }

                //the client updates the journey state if pickup/destination/other have been reached
                if (account.getType() == UserType.CLIENT)
                    updateJourneyStateBasedOnPosition(journey, dto, lastKnownLocationInfo);

                return new UBoatDTO(UBoatStatus.SAIL_RECORDED, new SailDTO(journey, lastKnownLocationInfo));
            } finally {
                recordLocation(account, journey, dto);
            }
        } catch (Exception e) {
            log.error("An exception occurred during sail workflow.", e);
            return new UBoatDTO(UBoatStatus.VAULT_INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public UBoatDTO getMostRecentRides(String authorizationHeader, Integer ridesRequested) {
        try {
            //cant be null because the operation is already done in the filter before
            var jwtData = jwtService.extractUsernameAndPhoneNumberFromHeader(authorizationHeader);
            var account = entityService.findAccountByJwtData(jwtData);

            var foundJourneys = journeyRepository.findAllByClientAccount_IdAndState(account.getId(), JourneyState.SUCCESSFULLY_FINISHED);
            if (foundJourneys == null || foundJourneys.isEmpty()) {
                log.info("User has no completed journeys.");
                return new UBoatDTO(UBoatStatus.MOST_RECENT_RIDES_RETRIEVED, new LinkedList<>());
            }

            log.info("Found completed {} journeys for the user.", foundJourneys.size());
            var journeys = foundJourneys.stream()
                    .map(JourneyDTO::buildDTOForClients)
                    .sorted(Comparator.comparingInt(j -> (int) DateUtils.getSecondsPassed(j.getTemporalData().getDateArrival())))
                    .limit(ridesRequested)
                    .toList();

            return new UBoatDTO(UBoatStatus.MOST_RECENT_RIDES_RETRIEVED, journeys);
        } catch (Exception e) {
            log.error("An exception occurred during getMostRecentRides workflow.", e);
            return new UBoatDTO(UBoatStatus.VAULT_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * This method updates the sailor account locationData and lastUpdate to the current system time in order to mark this sailor as active.
     */
    @Transactional
    public UBoatDTO pulse(String authorizationHeader, PulseDTO pulseRequest) {
        try {
            //cant be null because the operation is already done in the filter before
            var jwtData = jwtService.extractUsernameAndPhoneNumberFromHeader(authorizationHeader);
            var sailor = entityService.findSailorByJwt(jwtData);

            var journeyOptional = journeyRepository.findSailorJourneyMatchingAccountAndState(sailor.getAccount().getId(), Set.of(JourneyState.SAILING_TO_CLIENT, JourneyState.SAILING_TO_DESTINATION, JourneyState.VERIFYING_PAYMENT));

            if (journeyOptional.isPresent()) pulseRequest.setLookingForClients(false);

            var oldLocationData = sailor.getCurrentLocation();
            sailor.setCurrentLocation(pulseRequest.getLocationData());
            sailor.setLookingForClients(pulseRequest.isLookingForClients());

            sailorsRepository.save(sailor);
            log.info("Updated sailor location data and isLookingForClients status to {} via pulse.", pulseRequest.isLookingForClients());

            if (oldLocationData != null) {
                locationDataRepository.deleteById(oldLocationData.getId());
                log.debug("Deleted old location data.");
            }

            if (journeyOptional.isPresent())
                return new UBoatDTO(UBoatStatus.PULSE_JOURNEY_DETECTED, true);

            return new UBoatDTO(UBoatStatus.PULSE_SUCCESSFUL, sailor.isLookingForClients());
        } catch (Exception e) {
            log.error("An exception occurred during pulse workflow.", e);
            return new UBoatDTO(UBoatStatus.VAULT_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * This method build a Route object for each<br>
     * The calculation is done by summing the distance calculated by the algorithm between the sailor boat to the client plus the distance between the client to the destination.
     */
    private Journey buildJourney(JourneyRequestDTO journeyRequestDTO, Sailor sailor, Account clientAccount) {
        var route = new Route(new Location(sailor.getCurrentLocation()), journeyRequestDTO);

        try {
            route.calculateRoute(geoService);
        } catch (NoRouteFoundException exception) {
            log.warn("No possible route on water was found for the sailor's position, client and destination coordinates.");
            return null;
        } catch (Exception e) {
            log.error("Failed to create route for sailor.", e);
            return null;
        }

        var estimatedDuration = geoService.estimateRideDurationInSeconds(route.getTotalDistance(geoService), sailor.getBoat());

        var journey = Journey.builder().clientAccount(clientAccount).sailor(sailor).state(JourneyState.INITIATED).route(route).journeyTemporalData(new JourneyTemporalData(estimatedDuration)).build();

        journey.setPayment(new Payment(journey, geoService.estimateRideCost(route.getTotalDistance(), sailor.getBoat())));
        log.info("Possible journey found.");
        return journey;
    }


    /**
     * For the given sailor updates its lookingForClients status to false if the sailor has not been active in the last MAX_ACTIVE_SECONDS seconds.
     */
    @Transactional
    public void checkAndUpdateSailorActiveStatus(Sailor sailor) {
        if (!sailor.isLookingForClients()) return;

        if (DateUtils.getSecondsPassed(sailor.getLastUpdate()) < MAX_ACTIVE_SECONDS) return;

        sailor.setLookingForClients(false, false);
        log.info("Sailor '{}' has not been active in the last {} seconds. Updating lookingForClient flag to false.", sailor.getAccount().getUsername(), MAX_ACTIVE_SECONDS);
    }

    private CreditCard findCreditCardMatchingNumberForClientAccount(Account clientAccount, String number) {
        return clientAccount.getCreditCards()
                .stream()
                .filter(card -> card.getNumber().trim().equalsIgnoreCase(number.trim()))
                .findFirst()
                .orElse(null);
    }

    /**
     * This method creates new journeys in state INITIATED if:
     * <ol>
     *     <li>There were free active sailors found. A sailor is active if he has sent a pulse to set his last update field. This method also triggers a check and update(if necessary) for the isLookingForClients field of the sailors.</li>
     *     <li>For each of the sailors extracted at the previous step, this method will call GeoService to create routes and do the cost/distance calculation for each sailor found based on the request source/destination coordinates.
     *     Any errors encountered during this process will just ignore the sailor and continue with the next one. However, if no errors occurred, a new Journey object will be created.</li>
     * </ol>
     *
     * @return If no route could be determined or there are no free sailors, an empty list will be returned, otherwise a JourneyDTO containing all the required information about the journey (including Polyline coordinates) will be returned
     */
    @Transactional
    public UBoatDTO requestJourney(String authorizationHeader, JourneyRequestDTO dto) {
        try {
            log.info("Searching for sailors...");

            //  cant be null because the operation is already done in the filter before
            var jwtData = jwtService.extractUsernameAndPhoneNumberFromHeader(authorizationHeader);
            log.debug("JWT data extracted.");

            var clientAccount = entityService.findAccountByJwtData(jwtData);

            //  dismiss all current CLIENT_ACCEPTED journeys
            journeyRepository.deleteAllByClientAccountAndStateIn(clientAccount, Set.of(JourneyState.INITIATED, JourneyState.CLIENT_ACCEPTED));


            var activeSailors = sailorsRepository.findAllByLookingForClients(true);
            activeSailors.forEach(this::checkAndUpdateSailorActiveStatus);

            var sailors = activeSailors.stream().filter(Sailor::isLookingForClients)//  - removed sailors who are not active in the last accepted time frame
                    .filter(sailor -> geoService.calculateDistanceBetweenCoordinates(new LatLng(sailor.getCurrentLocation()), dto.getPickupLocation().getCoordinates()) < MAX_ACCEPTED_DISTANCE)//  - removes sailors that are not in at least MAX_ACCEPTED_DISTANCE meters between them and the pickup location
                    .toList();

            if (sailors.isEmpty()) {
                log.info("There are no free sailors, withing distance, active in the last {} seconds.", MAX_ACTIVE_SECONDS);
                return new UBoatDTO(UBoatStatus.NO_FREE_SAILORS_FOUND, new LinkedList<>());
            }

            log.info("{} free sailors within distance were found. ", sailors.size());

            var journeys = sailors.stream().map(sailor -> this.buildJourney(dto, sailor, clientAccount))  //build Journey entities
                    .filter(Objects::nonNull)   //remove error journeys
                    .sorted((j1, j2) -> (int) (j1.getRoute().getTotalDistance() - j2.getRoute().getTotalDistance()))    //sort by total distance ascending
                    .limit(MAX_ACTIVE_SAILORS)  //create only MAX_ACTIVE_SAILORS journeys
                    .toList();

            if (journeys.isEmpty()) {
                log.warn("{} free sailors were found but failed to create any journey for them and the client's location->destination route.", sailors.size());
                return new UBoatDTO(UBoatStatus.NO_ROUTE_FOUND, new LinkedList<>());
            }

            journeyRepository.saveAll(journeys);
            log.info("Created {} new INITIATED journeys.", journeys.size());

            return new UBoatDTO(UBoatStatus.JOURNEYS_INITIATED, journeys.stream().map(JourneyDTO::buildDTOForClients).toList());
        } catch (Exception e) {
            log.error("An exception occurred during requestJourney workflow.", e);
            return new UBoatDTO(UBoatStatus.VAULT_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * This method establishes the connection between the client and the sailor after the client has chosen the sailorId sent from the request body.<br>
     * It validates that the data in the request (the sailor is online, and it is not already having another journey) then puts the Journey in
     * stage CLIENT_ACCEPTED, dismissing all the other journeys he has in stage INITIATED into CLIENT_CANCELED.
     */
    @Transactional
    public UBoatDTO chooseJourney(String authorizationHeader, JourneyDTO journeyDTO) {
        try {
            //cant be null because the operation is already done in the filter before
            var jwtData = jwtService.extractUsernameAndPhoneNumberFromHeader(authorizationHeader);
            var clientAccount = entityService.findAccountByJwtData(jwtData);

            //first check if the payment method is valid
            var paymentType = PaymentType.fromPaymentType(journeyDTO.getPayment().getPaymentType());
            CreditCard creditCard = null;
            if (paymentType == PaymentType.CARD) {
                creditCard = findCreditCardMatchingNumberForClientAccount(clientAccount, journeyDTO.getPayment().getCardNumber());
                if (creditCard == null) {
                    log.warn("Credit card could not be found for the given account.");
                    return new UBoatDTO(UBoatStatus.PAYMENT_METHOD_NOT_FOUND, null);
                }
            }

            var sailor = sailorsRepository.findSailorByIdAndLookingForClientsIsTrue(journeyDTO.getSailorDetails().getSailorId());

            //if the sailor could not be found, or he has not updated his status for more than MAX_ACTIVE_SECONDS
            if (sailor == null || DateUtils.getSecondsPassed(sailor.getLastUpdate()) >= MAX_ACTIVE_SECONDS) {
                log.warn("Couldn't find the sailor. Either the sailor is busy, not active or the client request is wrong.");
                return new UBoatDTO(UBoatStatus.SAILOR_NOT_FOUND);
            }

            var journeys = journeyRepository.findAllByClientAccount_IdAndState(clientAccount.getId(), JourneyState.INITIATED);

            var journeyOptional = journeys.stream().filter(j -> j.getSailor().getId().equals(journeyDTO.getSailorDetails().getSailorId()) && j.getClientAccount().getId().equals(clientAccount.getId())).findFirst();

            if (journeyOptional.isEmpty()) {
                log.warn("No journey could be found with the request's journey sailor ID for the authenticated client.");
                return new UBoatDTO(UBoatStatus.JOURNEY_FOR_SAILOR_NOT_FOUND, false);
            }

            var journey = journeyOptional.get();

            //cancel other journeys
            journeys.stream().filter(otherJourney -> !otherJourney.equals(journey)).forEach(journeyToBeCanceled -> {
                journeyToBeCanceled.setState(JourneyState.CLIENT_CANCELED);
                journeyRepository.save(journeyToBeCanceled);
                log.info("Journey with ID {} was canceled by the user.", journeyToBeCanceled.getId());
            });

            journey.setState(JourneyState.CLIENT_ACCEPTED);
            journey.getPayment().setPaymentType(paymentType);
            journey.getPayment().setCreditCard(creditCard);

            journeyRepository.save(journey);

            return new UBoatDTO(UBoatStatus.CLIENT_ACCEPTED_JOURNEY, true);
        } catch (Exception e) {
            log.error("Exception occurred during selectClient workflow.", e);
            return new UBoatDTO(UBoatStatus.VAULT_INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public UBoatDTO deleteInitiatedJourneys(String authorizationHeader) {
        try {
            //cant be null because the operation is already done in the filter before
            var jwtData = jwtService.extractUsernameAndPhoneNumberFromHeader(authorizationHeader);
            var clientAccount = entityService.findAccountByJwtData(jwtData);

            journeyRepository.deleteAllByClientAccountAndStateIn(clientAccount, Set.of(JourneyState.INITIATED));

            return new UBoatDTO(UBoatStatus.INITIATED_JOURNIES_DELETED, true);
        } catch (UBoatJwtException e) {
            log.error("Exception occurred during cancelJourney workflow.", e);
            return new UBoatDTO(UBoatStatus.VAULT_INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public UBoatDTO hasSailorAcceptedJourney(String authorizationHeader, Long sailorId) {
        try {
            //cant be null because the operation is already done in the filter before
            var jwtData = jwtService.extractUsernameAndPhoneNumberFromHeader(authorizationHeader);
            var account = entityService.findAccountByJwtData(jwtData);

            var journeys = journeyRepository.findAllByStateAndSailorId(JourneyState.SAILOR_ACCEPTED, sailorId);
            for (var journey : journeys)
                if (journey.getClientAccount().getId().equals(account.getId())) {
                    log.info("Journey for the client and sailor with state SAILOR_ACCEPTED has been found.");
                    journey.setState(JourneyState.SAILING_TO_CLIENT);
                    journey.getJourneyTemporalData().setDateBooking(new Date());
                    journeyRepository.save(journey);
                    return new UBoatDTO(UBoatStatus.JOURNEY_WITH_STATE_FOUND, true);
                }

            log.info("Journey for the client and sailor with state SAILOR_ACCEPTED has NOT been found.");
            return new UBoatDTO(UBoatStatus.JOURNEY_WITH_STATE_NOT_FOUND, false);
        } catch (Exception e) {
            log.error("Exception occurred during findClient workflow. Returning null.", e);
            return null;
        }
    }

    /**
     * This method searches for journey objects that have status CLIENT_ACCEPTED for the sailor from request and returns their JourneysDTO.
     */
    @Transactional
    public UBoatDTO findClient(String authorizationHeader) {
        try {
            //cant be null because the operation is already done in the filter before
            var jwtData = jwtService.extractUsernameAndPhoneNumberFromHeader(authorizationHeader);
            var sailor = entityService.findSailorByJwt(jwtData);

            if (!sailor.isLookingForClients()) {
                sailor.setLookingForClients(true);
                sailorsRepository.save(sailor);
            }

            var journeys = journeyRepository.findAllByStateAndSailorId(JourneyState.CLIENT_ACCEPTED, sailor.getId());

            if (CollectionUtils.isEmpty(journeys)) return new UBoatDTO(UBoatStatus.NO_CLIENT_FOUND);

            var closestJourneyToClientOptional = journeys.stream().min((j1, j2) -> Double.compare(j1.getRoute().getDistanceBetweenSailorAndClient(geoService), j2.getRoute().getDistanceBetweenSailorAndClient(geoService)));

            if (closestJourneyToClientOptional.isEmpty()) throw new RuntimeException("Empty optional found.");

            var closestJourneyToClient = closestJourneyToClientOptional.get();
            return new UBoatDTO(UBoatStatus.CLIENT_FOUND, JourneyDTO.buildDTOForSailors(closestJourneyToClient));
        } catch (Exception e) {
            log.error("Exception occurred during findClient workflow. Returning null.", e);
            return null;
        }
    }

    /**
     * This method changed the state of the request's client journey to SAILOR_ACCEPTED and dismisses the other Journeys with state CLIENT_ACCEPTED into SAILOR_CANCELED.
     */
    @Transactional
    public UBoatDTO confirmClient(String authorizationHeader, JourneyDTO journeyDTO) {
        try {
            //cant be null because the operation is already done in the filter before
            var jwtData = jwtService.extractUsernameAndPhoneNumberFromHeader(authorizationHeader);
            var sailor = entityService.findSailorByJwt(jwtData);

            var journeyOptional = journeyRepository.findBySailorAndState(sailor, JourneyState.SAILOR_ACCEPTED);
            if (journeyOptional.isPresent()) {
                log.warn("Another journey has already been selected");
                return new UBoatDTO(UBoatStatus.JOURNEY_CONFIRMED, JourneyDTO.buildDTOForSailors(journeyOptional.get()));
            }

            var pickupLocationCoordinates = journeyDTO.getRoute().getPickupLocation().getCoordinates();
            var destinationLocationCoordinates = journeyDTO.getRoute().getDestinationLocation().getCoordinates();

            var journey = journeyRepository.findJourneyOfSailorMatchingStatePickupAndDestination(JourneyState.CLIENT_ACCEPTED, sailor.getId(), pickupLocationCoordinates.getLatitude(), pickupLocationCoordinates.getLongitude(), destinationLocationCoordinates.getLatitude(), destinationLocationCoordinates.getLongitude());

            if (journey == null) return new UBoatDTO(UBoatStatus.JOURNEY_NOT_FOUND, null);

            log.info("Found journey from the request with status CLIENT_ACCEPTED. Journey id: {}", journey.getId());

            //dismiss any other journey for the current sailor - set all the other CLIENT_ACCEPTED journeys for this sailor to SAILOR_CANCELED
            var otherJourneys = journeyRepository.findAllByStateAndSailorId(JourneyState.CLIENT_ACCEPTED, sailor.getId());
            if (!CollectionUtils.isEmpty(otherJourneys)) {
                log.info("Canceling other journeys.");
                otherJourneys.stream().filter(otherJourney -> !otherJourney.getId().equals(journey.getId())).forEach(otherJourney -> {
                    otherJourney.setState(JourneyState.SAILOR_CANCELED);
                    journeyRepository.save(otherJourney);
                    log.info("Canceled journey with id {}.", otherJourney.getId());
                });
            }

            journey.setState(JourneyState.SAILOR_ACCEPTED);
            journeyRepository.save(journey);
            log.info("Status of journey changed from CLIENT_ACCEPTED to SAILOR_ACCEPTED. Journey id: {}", journey.getId());

            return new UBoatDTO(UBoatStatus.JOURNEY_CONFIRMED, JourneyDTO.buildDTOForSailors(journey));
        } catch (Exception e) {
            log.error("Exception occurred during selectClient workflow.", e);
            return new UBoatDTO(UBoatStatus.VAULT_INTERNAL_SERVER_ERROR);
        }
    }
}
