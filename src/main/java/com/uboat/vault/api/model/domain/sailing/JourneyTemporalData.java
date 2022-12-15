package com.uboat.vault.api.model.domain.sailing;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class JourneyTemporalData {
    @NotNull
    @Getter
    @Setter
    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date dateInitiated;

    @Getter
    @Setter
    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date dateBooking;

    @Getter
    @Setter
    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date dateArrival;

    @Column(nullable = false)
    @Getter
    @Setter
    private int estimatedDurationSeconds;

    public JourneyTemporalData(int estimatedDurationSeconds) {
        this.dateInitiated = new Date();
        this.estimatedDurationSeconds = estimatedDurationSeconds;
    }
}
