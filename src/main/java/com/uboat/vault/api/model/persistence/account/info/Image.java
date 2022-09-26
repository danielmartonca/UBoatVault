package com.uboat.vault.api.model.persistence.account.info;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.uboat.vault.api.utilities.HashUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Images")
public class Image {
    private static final Logger log = LoggerFactory.getLogger(Image.class);

    @JsonIgnore
    @Id
    @Getter
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    private Long id;

    @Getter
    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Type(type = "org.hibernate.type.ImageType")
    private byte[] bytes;
    @Getter
    private String hash;

    @JsonIgnore
    @Getter
    @Setter
    @OneToOne()
    @JoinColumn(name = "account_details_id")
    private AccountDetails accountDetails;

    public Image(AccountDetails accountDetails) {
        this.accountDetails = accountDetails;
    }

    @Override
    public String toString() {
        return "{" + "bytes:" + '"' + bytes.length + " bytes\"" + '}';
    }

    private void calculateHash() {
        if (bytes == null) {
            log.debug("Image bytes are null. Cannot calculate hash.");
            return;
        }
        hash = HashUtils.calculateHash(this.bytes);
        log.debug("Calculated image hash successfully.");
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
        calculateHash();
    }
}
