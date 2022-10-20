package com.uboat.vault.api.model.domain.sailing.sailor;

import com.uboat.vault.api.model.domain.account.Account;
import com.uboat.vault.api.model.domain.sailing.LocationData;
import com.uboat.vault.api.model.dto.LocationDataDTO;
import com.uboat.vault.api.model.enums.UserType;
import com.uboat.vault.api.utilities.LoggingUtils;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

@NoArgsConstructor
@Entity
@Table(name = "Sailors")
public class Sailor {
    @Id
    @Getter
    @Setter
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    private Long id;

    @Getter
    @Setter
    @Column(nullable = false, unique = true)
    private Long accountId;

    @Getter
    @Setter
    private boolean lookingForClients;

    @Getter
    @Setter
    @Column(name = "last_update")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUpdate;
    @Getter
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "location_data_id")
    private LocationData currentLocation;

    @NotNull
    @Getter
    @Setter
    @OneToOne(cascade = {CascadeType.ALL})
    @JoinColumn(name = "boat_id")
    private Boat boat;

    @Getter
    @Setter
    private double averageRating;

    public Sailor(Account account) {
        if (account.getType() != UserType.SAILOR)
            throw new RuntimeException("Account given as parameter is not a sailor account");

        this.accountId = account.getId();
        this.boat = new Boat(this);
        this.lookingForClients = false;
        this.lastUpdate = null;
        this.currentLocation = null;
    }

//    @Getter
//    @Setter
//    @OneToMany(mappedBy = "sailor", cascade = {CascadeType.REMOVE, CascadeType.MERGE})
//    private Set<Ranking> rankings;

    @Override
    public String toString() {
        return LoggingUtils.toStringFormatted(this);
    }

    public void setCurrentLocation(LocationDataDTO currentLocation) {
        this.currentLocation = new LocationData(currentLocation);
    }
}
