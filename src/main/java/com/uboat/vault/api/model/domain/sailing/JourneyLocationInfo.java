package com.uboat.vault.api.model.domain.sailing;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.uboat.vault.api.model.enums.JourneyState;
import com.uboat.vault.api.model.enums.UserType;
import lombok.*;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;

@Entity
@Table(name = "JourneyLocationInfo")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JourneyLocationInfo {
    @JsonIgnore
    @Id
    @Getter
    @Setter
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Getter
    private UserType recorder;

    @Enumerated(EnumType.STRING)
    @Getter
    private JourneyState journeyState;

    public void setJourneyState(JourneyState journeyState) {
        if (!Set.of(JourneyState.SAILING_TO_CLIENT, JourneyState.SAILING_TO_DESTINATION).contains(journeyState))
            throw new RuntimeException("Stage '" + journeyState.name() + "' is not allowed for JourneyLocationInfo.");
        this.journeyState = journeyState;
    }

    @Embedded
    @Getter
    @Setter
    private Location location;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @Getter
    @Setter
    private LocationData locationData;

    @Getter
    @Setter
    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp;

    @Getter
    @Setter
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "journey_id", nullable = false)
    private Journey journey;
}
