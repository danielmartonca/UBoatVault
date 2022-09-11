package com.uboat.vault.api.model.persistence.sailing.sailor;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Arrays;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "BoatImages")
public class BoatImage {
    @JsonIgnore
    @Id
    @Getter
    @Setter
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    private Long id;

    @Getter
    @Setter
    @Lob
    @Basic(fetch = FetchType.EAGER)
    private byte[] bytes;

    @JsonIgnore
    @Getter
    @Setter
    @ManyToOne()
    @JoinColumn(name = "boat_id", nullable = false)
    private Boat boat;

    public BoatImage(byte[] bytes, Boat boat) {
        this.bytes = bytes;
        this.boat = boat;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BoatImage boatImage = (BoatImage) o;
        return Arrays.equals(bytes, boatImage.bytes);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(bytes);
    }
}
