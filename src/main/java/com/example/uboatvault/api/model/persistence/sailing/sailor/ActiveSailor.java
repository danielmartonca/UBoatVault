package com.example.uboatvault.api.model.persistence.sailing.sailor;

import com.example.uboatvault.api.model.persistence.sailing.LocationData;
import com.example.uboatvault.api.utilities.LoggingUtils;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.Set;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "ActiveSailors")
public class ActiveSailor {
    @Id
    @Getter
    @Setter
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    private Long id;

    @Getter
    @Setter
    private Long accountId;

    @Getter
    @Setter
    @Column(name = "last_update")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUpdate;

    @Getter
    @Setter
    private boolean lookingForClients = false;

    @Getter
    @Setter
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "location_data_id")
    private LocationData locationData;

    @NotNull
    @Getter
    @Setter
    @OneToOne(cascade = {CascadeType.ALL})
    @JoinColumn(name = "boat_id")
    private Boat boat;

    @Getter
    @Setter
    private double averageRating;

    @Getter
    @Setter
    @OneToMany(mappedBy = "activeSailor", cascade = {CascadeType.REMOVE, CascadeType.MERGE})
    private Set<Ranking> rankings;

    @Override
    public String toString() {
        return LoggingUtils.toStringFormatted(this);
    }
}
