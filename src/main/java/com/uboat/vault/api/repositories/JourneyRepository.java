package com.uboat.vault.api.repositories;

import com.uboat.vault.api.model.enums.Stage;
import com.uboat.vault.api.model.persistence.sailing.Journey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface JourneyRepository extends JpaRepository<Journey, Long> {
    List<Journey> findAllByClientAccount_IdAndStatus(Long clientAccountId, Stage status);

    List<Journey> findAllByStatusAndSailorAccount_Id(Stage status, Long sailorAccountId);

    @Query("SELECT j from Journey  j where j.status=?1 and j.sailorAccount.id=?2 and j.sourceLatitude=?3 and j.sourceLongitude=?4 and j.destinationLatitude=?5 and j.destinationLongitude=?6")
    Journey findNewJourneyOfSailorMatchingSourceAndDestination(Stage status, Long sailorId, double sourceLatitude, double sourceLongitude, double destinationLatitude, double destinationLongitude);
}
