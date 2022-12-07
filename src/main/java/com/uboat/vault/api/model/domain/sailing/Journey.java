package com.uboat.vault.api.model.domain.sailing;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.uboat.vault.api.model.domain.account.account.Account;
import com.uboat.vault.api.model.domain.account.sailor.Sailor;
import com.uboat.vault.api.model.enums.JourneyState;
import lombok.*;

import javax.persistence.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "Journeys")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Journey {
    @JsonIgnore
    @Id
    @Getter
    @Setter
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    private Long id;

    @JsonIgnore
    @Getter
    @Setter
    @ManyToOne()
    @JoinColumn(name = "client_account_id")
    private Account clientAccount;

    @JsonIgnore
    @Getter
    @Setter
    @ManyToOne
    @JoinColumn(name = "sailor_id")
    private Sailor sailor;

    @JsonIgnore
    @Getter
    @Setter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JourneyState status;

    @Getter
    @Setter
    @Embedded
    private Route route;

    @Embedded
    @Getter
    @Setter
    private JourneyTemporalData journeyTemporalData;

    @JsonIgnore
    @Getter
    @Setter
    @OneToOne(mappedBy = "journey")
    @JoinColumn(name = "payment_id", nullable = false, updatable = false, insertable = false)
    private Payment payment;

    @Getter
    @Setter
    @OneToMany(mappedBy = "journey", cascade = CascadeType.ALL)
    private List<JourneyLocationInfo> recordedLocationInfos = new LinkedList<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Journey journey)) return false;
        return getId().equals(journey.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
