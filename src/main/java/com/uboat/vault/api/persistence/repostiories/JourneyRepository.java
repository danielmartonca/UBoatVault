package com.uboat.vault.api.persistence.repostiories;

import com.uboat.vault.api.model.domain.sailing.Journey;
import com.uboat.vault.api.model.enums.JourneyState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface JourneyRepository extends JpaRepository<Journey, Long> {
    List<Journey> findAllByClientAccount_IdAndStatus(Long clientAccountId, JourneyState status);

    List<Journey> findAllByStatusAndSailorAccount_Id(JourneyState status, Long sailorAccountId);

    @Query("SELECT j from Journey  j where j.status=?1 and j.sailor.account.id=?2 and j.route.source.coordinates.latitude=?3 and j.route.source.coordinates.longitude=?4 and j.route.destination.coordinates.latitude=?5 and j.route.destination.coordinates.longitude=?6")
    Journey findNewJourneyOfSailorMatchingSourceAndDestination(JourneyState status, Long sailorId, double sourceLatitude, double sourceLongitude, double destinationLatitude, double destinationLongitude);
}
