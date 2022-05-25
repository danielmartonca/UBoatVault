package com.example.uboatvault.api.model.persistence.location;

import com.example.uboatvault.api.model.persistence.account.Account;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.Set;

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
    private String source;

    @Getter
    @Setter
    private String destination;

    @Getter
    @Setter
    private String payment;

    @Transient
    @Getter
    @Setter
    private String duration;

    @Transient
    @Getter
    @Setter
    private Long sailorId;

    @Getter
    @Setter
    @OneToMany(mappedBy = "journey", cascade = {CascadeType.ALL})
    private Set<LocationData> locationDataList;

    @JsonIgnore
    @Getter
    @Setter
    @ManyToOne
    @JoinColumn(name = "client_id")
    private Account client;

    @JsonIgnore
    @Getter
    @Setter
    @ManyToOne
    @JoinColumn(name = "sailor_id")
    private Account sailor;
}
