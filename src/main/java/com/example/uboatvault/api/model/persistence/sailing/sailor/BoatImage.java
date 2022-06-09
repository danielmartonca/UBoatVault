package com.example.uboatvault.api.model.persistence.sailing.sailor;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

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

    public BoatImage(byte[] bytes) {
        this.bytes = bytes;
    }
}
