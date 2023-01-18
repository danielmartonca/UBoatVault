package com.uboat.vault.api.model.dto;

import com.uboat.vault.api.model.domain.account.sailor.Sailor;
import com.uboat.vault.api.model.domain.sailing.Journey;
import com.uboat.vault.api.model.domain.sailing.JourneyTemporalData;
import com.uboat.vault.api.model.domain.sailing.Route;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JourneyDTO {
    private String state;
    private JourneySailorDetailsDTO sailorDetails;
    private JourneyTemporalData temporalData;
    private PaymentDTO payment;
    private Route route;

    private JourneyDTO(Journey journey, Sailor sailor) {
        this.state = journey.getState().name();
        this.sailorDetails = JourneySailorDetailsDTO.builder()
                .sailorId(sailor.getId())
                .username(sailor.getAccount().getUsername())
                .fullName(sailor.getAccount().getAccountDetails().getFullName())
                .phone(new PhoneNumberDTO(sailor.getAccount().getPhone()))
                .boat(new BoatDTO(sailor.getBoat()))
                .build();

        this.temporalData = journey.getJourneyTemporalData();
        this.payment = new PaymentDTO(journey.getPayment());
        this.route = journey.getRoute();
    }

    private JourneyDTO(Journey journey) {
        this.state = journey.getState().name();
        this.sailorDetails = null;
        this.temporalData = journey.getJourneyTemporalData();
        this.payment = new PaymentDTO(journey.getPayment());
        this.route = journey.getRoute();
    }

    public static JourneyDTO buildDTOForClients(Journey journey) {
        return new JourneyDTO(journey, journey.getSailor());
    }

    public static JourneyDTO buildDTOForSailors(Journey journey) {
        return new JourneyDTO(journey);
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JourneySailorDetailsDTO {
        private Long sailorId;

        private String username;
        private String fullName;

        private PhoneNumberDTO phone;

        private BoatDTO boat;
    }
}
