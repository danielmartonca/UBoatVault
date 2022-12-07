package com.uboat.vault.api.model.domain.sailing;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class JourneyTemporalData {
    @Column()
    @Getter
    @Setter
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateBooking;

    @Getter
    @Setter
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateArrival;

    @Column(nullable = false)
    @Getter
    @Setter
    private int estimatedDurationSeconds;

    public JourneyTemporalData(int estimatedDurationSeconds) {
        this.estimatedDurationSeconds = estimatedDurationSeconds;
    }
}
