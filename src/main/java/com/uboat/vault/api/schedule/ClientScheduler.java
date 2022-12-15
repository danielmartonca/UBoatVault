package com.uboat.vault.api.schedule;

import com.uboat.vault.api.model.enums.JourneyState;
import com.uboat.vault.api.persistence.repostiories.JourneyRepository;
import com.uboat.vault.api.utilities.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
@RequiredArgsConstructor
public class ClientScheduler {
    @Value("${uboat.schedulersCron.clientScheduler.initiatedJourneyExpirationSeconds}")
    private Integer initiatedJourneyExpirationSeconds;
    private final JourneyRepository journeyRepository;

    @Async
    @Scheduled(cron = "${uboat.schedulersCron.clientScheduler.deleteInactiveInitiatedJourneys}")
    @Transactional
    public void deleteInactiveInitiatedJourneys() {
        try {
            var journeys = journeyRepository.findJourneysByState(JourneyState.INITIATED);
            for (var journey : journeys)
                if (DateUtils.getSecondsPassed(journey.getJourneyTemporalData().getDateInitiated()) >= initiatedJourneyExpirationSeconds) {
                    log.info("Initiated journey with id {} has expired. Deleting entry.", journey.getId());
                    journeyRepository.delete(journey);
                }
        } catch (Exception e) {
            log.error("Exception occurred during deleteInactiveInitiatedJourneys scheduled task.", e);
        }
    }
}