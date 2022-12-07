package com.uboat.vault.api.persistence.repostiories;

import com.uboat.vault.api.model.domain.account.account.Account;
import com.uboat.vault.api.model.domain.account.sailor.Sailor;
import com.uboat.vault.api.model.domain.sailing.Journey;
import com.uboat.vault.api.model.enums.JourneyState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface JourneyRepository extends JpaRepository<Journey, Long> {
    List<Journey> findAllByClientAccount_IdAndState(Long clientAccountId, JourneyState state);

    List<Journey> findAllByStateAndSailorAccount_Id(JourneyState state, Long sailorAccountId);

    void deleteAllByClientAccountAndState(Account clientAccount, JourneyState state);

    Optional<Journey> findBySailorAndState(Sailor sailor, JourneyState state);
    @Query("SELECT j from Journey  j where j.state=?1 and j.sailor.account.id=?2 and j.route.source.coordinates.latitude=?3 and j.route.source.coordinates.longitude=?4 and j.route.destination.coordinates.latitude=?5 and j.route.destination.coordinates.longitude=?6")
    Journey findJourneyOfSailorMatchingStateSourceAndDestination(JourneyState state, Long sailorId, double sourceLatitude, double sourceLongitude, double destinationLatitude, double destinationLongitude);
}
