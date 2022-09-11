package com.uboat.vault.api.repositories;

import com.uboat.vault.api.model.persistence.sailing.Journey;
import com.uboat.vault.api.model.persistence.sailing.Stage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JourneyRepository extends JpaRepository<Journey, Long> {
    List<Journey> findAllByClient_IdAndStatus(Long client_id, Stage status);

    List<Journey> findAllBySailor_IdAndStatus(Long sailorId, Stage status);

    Journey findBySailor_IdAndStatusAndDestinationLatitudeAndDestinationLongitude(Long sailorId, Stage status, double destinationLatitude, double destinationLongitude);
}
