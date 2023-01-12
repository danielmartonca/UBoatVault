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

import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class JourneyScheduler {

    private final JourneyRepository journeyRepository;

    @Value("${uboat.schedulersCron.journeyScheduler.journeysNotConfirmedTimeoutSeconds}")
    Integer journeysNotConfirmedTimeoutSeconds;

    @Async
    @Scheduled(cron = "${uboat.schedulersCron.journeyScheduler.removeJourneysNotConfirmed}")
    @Transactional
    public void removeJourneysNotConfirmed() {
        try {
            //TODO maybe add sailor accepted later?
            var journeys = journeyRepository.findJourneysByStateIn(JourneyState.INITIATED, JourneyState.CLIENT_ACCEPTED);
            var journeysToBeDeleted = journeys.stream()
                    .filter(j -> DateUtils.getSecondsPassed(j.getJourneyTemporalData().getDateInitiated()) >= journeysNotConfirmedTimeoutSeconds)
                    .collect(Collectors.toList());
            if (!journeysToBeDeleted.isEmpty()) {
                journeyRepository.deleteAll(journeysToBeDeleted);
                log.info("A total of {} journey(s) have been deleted due to no activity being detected in the last {} seconds.", journeysToBeDeleted.size(), journeysNotConfirmedTimeoutSeconds);
            }
        } catch (Exception e) {
            log.error("Exception occurred during removeJourneysNotConfirmed scheduled task.", e);
        }
    }
}