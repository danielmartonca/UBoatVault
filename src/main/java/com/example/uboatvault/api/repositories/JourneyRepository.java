package com.example.uboatvault.api.repositories;

import com.example.uboatvault.api.model.persistence.sailing.Journey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JourneyRepository extends JpaRepository<Journey, Long> {
    List<Journey> findAllByClient_IdAndDateArrivalNotNullOrderByDateBookingAsc(Long client_id);
}
