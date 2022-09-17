package com.uboat.vault.api.model.http.new_requests;

import com.uboat.vault.api.model.persistence.sailing.Journey;
import com.uboat.vault.api.model.persistence.sailing.Stage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestJourney {
    private Stage status;
    private Date dateBooking;
    private Date dateArrival;
    private double sourceLatitude;
    private double sourceLongitude;
    private String sourceAddress;
    private double destinationLatitude;
    private double destinationLongitude;
    private String destinationAddress;
    private String payment;
    private String duration;
    private Long sailorId;

    public RequestJourney(Journey journey) {
        this.status = journey.getStatus();

        this.dateBooking = journey.getDateBooking();
        this.dateArrival = journey.getDateArrival();
        calculateDuration();

        this.sourceLatitude = journey.getSourceLatitude();
        this.sourceLongitude = journey.getSourceLongitude();
        this.sourceAddress = journey.getSourceAddress();

        this.destinationLatitude = journey.getDestinationLatitude();
        this.destinationLongitude = journey.getDestinationLongitude();
        this.destinationAddress = journey.getDestinationAddress();

        this.payment = journey.getPayment();
        this.sailorId = journey.getSailor().getId();
    }

    public void calculateDuration() {
        if (this.dateBooking != null && this.dateArrival != null) {
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
