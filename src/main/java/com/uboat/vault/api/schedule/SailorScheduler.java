package com.uboat.vault.api.schedule;

import com.uboat.vault.api.business.services.JourneyService;
import com.uboat.vault.api.persistence.repostiories.SailorsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
@RequiredArgsConstructor
public class SailorScheduler {
    private final JourneyService journeyService;

    private final SailorsRepository sailorsRepository;

    @Async
    @Scheduled(cron = "*/30 * * * * *")
    @Transactional
    public void assertSailorsAreActive() {
        try {
            log.info("SailorScheduler assertSailorsAreActive task started. ");
            sailorsRepository.findAllByLookingForClients(true).forEach(journeyService::checkAndUpdateSailorActiveStatus);
            log.info("SailorScheduler assertSailorsAreActive task finished.");
        } catch (Exception e) {
            log.error("Exception occurred during assertSailorsAreActive sheduled task.", e);
        }
    }
}
