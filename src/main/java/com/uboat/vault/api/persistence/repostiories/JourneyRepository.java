package com.uboat.vault.api.persistence.repostiories;

import com.uboat.vault.api.model.domain.account.account.Account;
import com.uboat.vault.api.model.domain.account.sailor.Sailor;
import com.uboat.vault.api.model.domain.sailing.Journey;
import com.uboat.vault.api.model.enums.JourneyState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import javax.persistence.LockModeType;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface JourneyRepository extends JpaRepository<Journey, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT j from Journey  j where j.clientAccount.id=?1 and j.state IN ?2")
    Optional<Journey> findClientJourneyMatchingAccountAndState(Long clientAccountId, Iterable<JourneyState> states);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT j from Journey  j where  j.sailor.account.id=?1 and j.state IN ?2")
    Optional<Journey> findSailorJourneyMatchingAccountAndState(Long sailorAccountId, Iterable<JourneyState> states);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Journey> findJourneysByState(JourneyState state);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Journey> findJourneysByStateIn(Collection<JourneyState> states);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Journey> findAllByClientAccount_IdAndState(Long clientAccountId, JourneyState state);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Journey> findAllByStateAndSailorId(JourneyState state, Long sailorId);

    void deleteAllByClientAccountAndStateIn(Account clientAccount, Set<JourneyState> state);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Journey> findBySailorAndState(Sailor sailor, JourneyState state);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT j from Journey  j where j.state=?1 and j.sailor.id=?2 and j.route.pickupLocation.coordinates.latitude=?3 and j.route.pickupLocation.coordinates.longitude=?4 and j.route.destinationLocation.coordinates.latitude=?5 and j.route.destinationLocation.coordinates.longitude=?6")
    Journey findJourneyOfSailorMatchingStatePickupAndDestination(JourneyState state, Long sailorId, double pickupLocationLatitude, double pickupLocationLongitude, double destinationLocationLatitude, double destinationLocationLongitude);
}
