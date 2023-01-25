package com.uboat.vault.api.schedule;

import com.uboat.vault.api.business.services.JourneyService;
import com.uboat.vault.api.model.enums.JourneyState;
import com.uboat.vault.api.model.enums.UserType;
import com.uboat.vault.api.persistence.repostiories.JourneyRepository;
import com.uboat.vault.api.utilities.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class JourneyScheduler {
    private final JourneyService journeyService;
    private final JourneyRepository journeyRepository;

    @Value("${uboat.schedulersCron.journeyScheduler.journeysNotConfirmedTimeoutSeconds:60}")
    Integer journeysNotConfirmedTimeoutSeconds;
    @Value("${uboat.schedulersCron.journeyScheduler.checkNoActivityJourneysTimeoutSeconds:600}")
    Integer checkNoActivityJourneysTimeoutSeconds;

    @Async
    @Scheduled(cron = "${uboat.schedulersCron.journeyScheduler.removeJourneysNotConfirmed:-}")
    @Transactional
    public void removeJourneysNotConfirmed() {
        try {
            var journeys = journeyRepository.findJourneysByStateIn(List.of(JourneyState.INITIATED, JourneyState.CLIENT_ACCEPTED));
            var journeysToBeSetInError = journeys.stream()
                    .filter(j -> DateUtils.getSecondsPassed(j.getJourneyTemporalData().getDateInitiated()) >= journeysNotConfirmedTimeoutSeconds)
                    .toList();

            if (!journeysToBeSetInError.isEmpty()) {
                journeysToBeSetInError.forEach(j -> {
                    journeyService.setJourneyInError(j, "Journey no activity being detected in the last [journeysNotConfirmedTimeoutSeconds] seconds.");
                    log.warn("Journey with ID {} has been set in error due to no activity being detected in the last {} seconds.", j.getId(), journeysNotConfirmedTimeoutSeconds);
                    journeyRepository.save(j);
                });
                log.warn("A total of {} journey(s) have been deleted in the removeJourneysNotConfirmed task scheduler due to no activity being detected in the last {} seconds.", journeysToBeSetInError.size(), journeysNotConfirmedTimeoutSeconds);
            }
        } catch (Exception e) {
            log.error("Exception occurred during removeJourneysNotConfirmed scheduled task.", e);
        }
    }

    @Async
    @Scheduled(cron = "${uboat.schedulersCron.journeyScheduler.checkNoActivityJourneys:-}")
    @Transactional
    public void checkNoActivityJourneys() {
        try {
            var journeys = journeyRepository.findJourneysByStateIn(List.of(JourneyState.SAILING_TO_CLIENT, JourneyState.SAILING_TO_DESTINATION));
            var journeysToBeSetInError = journeys.stream()
                    .filter(j -> {
                        var lastKnownClientLocation = j.getLastKnownLocation(UserType.CLIENT);
                        var lastKnownSailorLocation = j.getLastKnownLocation(UserType.SAILOR);

                        if (lastKnownClientLocation == null && lastKnownSailorLocation == null && DateUtils.getSecondsPassed(j.getJourneyTemporalData().getDateInitiated()) >= checkNoActivityJourneysTimeoutSeconds) {
                            log.warn("Journey with ID {} does not have any activity at all in {} seconds since initiated.", j.getId(), checkNoActivityJourneysTimeoutSeconds);
                            return true;
                        }

                        if (lastKnownClientLocation == null || lastKnownSailorLocation == null) return false;

                        if (DateUtils.getSecondsPassed(lastKnownClientLocation.getTimestamp()) >= checkNoActivityJourneysTimeoutSeconds && DateUtils.getSecondsPassed(lastKnownSailorLocation.getTimestamp()) >= checkNoActivityJourneysTimeoutSeconds) {
                            log.warn("Journey with ID {} does not have any activity received from both the client and the sailor in {} seconds since initiated.", j.getId(), checkNoActivityJourneysTimeoutSeconds);
                            return true;
                        }
                        return false;
                    }).toList();
            if (!journeysToBeSetInError.isEmpty()) {
                journeysToBeSetInError.forEach(j -> journeyService.setJourneyInError(j, "Journey no activity being detected in the [checkNoActivityJourneysTimeoutSeconds] seconds seconds since its initiation."));
                log.warn("A total of {} journey(s) have been set in IN_ERROR state with checkNoActivityJourneys task scheduler due to no activity being detected in {} seconds since its(their) initiation.", journeysToBeSetInError.size(), checkNoActivityJourneysTimeoutSeconds);
            }
        } catch (Exception e) {
            log.error("Exception occurred during checkNoActivityJourneysTimeoutSeconds scheduled task.", e);
        }
    }

    @Async
    @Scheduled(cron = "${uboat.schedulersCron.journeyScheduler.completePayedJourneys:-}")
    @Transactional
    public void completePayedJourneys() {
        journeyRepository.findJourneysByStateIn(List.of(JourneyState.PAYMENT_VERIFIED))
                .parallelStream()
                .forEach(j -> {
                    try {
                        journeyService.completeSuccessfulJourney(j, 10);
                    } catch (InterruptedException e) {
                        log.error("Sleep interrupted while completing journey with id {}. Exception: {}", j.getId(), e);
                    }
                });

    }
}