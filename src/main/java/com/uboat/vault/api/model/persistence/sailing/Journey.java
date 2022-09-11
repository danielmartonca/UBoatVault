package com.uboat.vault.api.model.persistence.sailing;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.uboat.vault.api.model.persistence.account.Account;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
    @OneToMany(mappedBy = "journey", cascade = {CascadeType.ALL})
    private List<LocationData> locationDataList;

    @Transient
    @Getter
    @Setter
    private String duration;

    @Transient
    @Getter
    @Setter
    private Long sailorId;

    @JsonIgnore
    @Getter
    @Setter
    @ManyToOne()
    @JoinColumn(name = "client_id")
    private Account client;

    @JsonIgnore
    @Getter
    @Setter
    @ManyToOne
    @JoinColumn(name = "sailor_id")
    private Account sailor;

    public void calculateDuration() {
        if (dateBooking != null && dateArrival != null) {
            long diffInMilliseconds = Math.abs(dateArrival.getTime() - dateBooking.getTime());
            long minutes = TimeUnit.MILLISECONDS.toMinutes(diffInMilliseconds);
            diffInMilliseconds -= TimeUnit.MINUTES.toMillis(minutes);
            long seconds = TimeUnit.MILLISECONDS.toSeconds(diffInMilliseconds);
            this.duration = "";
            if (minutes != 0) this.duration = this.duration + minutes + " minutes";
            if (minutes != 0 && seconds != 0) this.duration = this.duration + " and ";
            if (seconds != 0) this.duration = this.duration + seconds + " seconds";
        } else
            this.duration = "has not arrived yet";
    }

}
