package com.example.uboatvault.api.model.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Entity
@Table(name = "PhoneNumbers")
public class PhoneNumber {
    @ToString.Include
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(nullable = false)
    private String phoneNumber;
    @Column(nullable = false)
    private String dialCode;
    @Column(nullable = false)
    private String isoCode;

    @JsonIgnore
    @OneToOne(cascade=CascadeType.ALL)
    private Account account;

    public PhoneNumber(String phoneNumber, String dialCode, String isoCode) {
        this.phoneNumber = phoneNumber;
        this.dialCode = dialCode;
        this.isoCode = isoCode;
    }

    @Override
    public String toString() {
        return "PhoneNumber{" +
                "phoneNumber='" + phoneNumber + '\'' +
                ", dialCode='" + dialCode + '\'' +
                ", isoCode='" + isoCode + '\'' +
                '}';
    }
}
