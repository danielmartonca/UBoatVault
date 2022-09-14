package com.uboat.vault.api.model.persistence.sailing.sailor;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.uboat.vault.api.utilities.HashUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "BoatImages")
public class BoatImage {
    private static final Logger log = LoggerFactory.getLogger(BoatImage.class);

    @JsonIgnore
    @Id
    @Getter
    @Setter
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    private Long id;

    @Getter
    @Lob
    @Basic(fetch = FetchType.EAGER)
    private byte[] bytes;

    @Getter
    private String hash;
    @JsonIgnore
    @Getter
    @Setter
    @ManyToOne()
    @JoinColumn(name = "boat_id", nullable = false)
    private Boat boat;

    public BoatImage(byte[] bytes, Boat boat) {
        this.boat = boat;
        setBytes(bytes);
    }

    @Override
    public String toString() {
        return "{" + "bytes:" + '"' + bytes.length + " bytes\"" + '}';
    }

    private void calculateHash() {
        if (bytes == null) {
            log.debug("Boat Image bytes are null. Cannot calculate hash.");
            return;
        }
        hash = HashUtils.calculateHash(this.bytes);
        log.debug("Calculated boat image hash successfully.");
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
        calculateHash();
    }
}
