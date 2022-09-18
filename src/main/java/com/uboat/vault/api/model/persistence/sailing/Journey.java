package com.uboat.vault.api.model.persistence.sailing;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.uboat.vault.api.model.enums.Stage;
import com.uboat.vault.api.model.persistence.account.Account;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

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
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Stage status;

    @Getter
    @Setter
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateBooking;

    @Getter
    @Setter
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateArrival;

    @Getter
    @Setter
    @NotNull
    private double sourceLatitude;

    @Getter
    @Setter
    @NotNull
    private double sourceLongitude;

    @Getter
    @Setter
    @NotNull
    private String sourceAddress;

    @Getter
    @Setter
    @NotNull
    private double destinationLatitude;

    @Getter
    @Setter
    @NotNull
    private double destinationLongitude;

    @Getter
    @Setter
    private String destinationAddress;

    @Getter
    @Setter
    private String payment;

    @Getter
    @Setter
    @JoinTable(name = "journeys_location_data")
    @OneToMany(cascade = {CascadeType.ALL})
    private List<LocationData> locationDataList;

    @Transient
    @Getter
    @Setter
    private String duration;

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
    @JoinColumn(name = "sailor_account_id")
    private Account sailorAccount;
}
