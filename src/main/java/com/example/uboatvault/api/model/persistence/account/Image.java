package com.example.uboatvault.api.model.persistence.account;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Images")
public class Image {
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
    @OneToOne()
    @JoinColumn(name = "account_details_id")
    private AccountDetails accountDetails;

    public Image(byte[] bytes) {
        this.bytes = bytes;
    }
}
