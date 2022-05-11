package com.example.uboatvault.api.model.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import java.util.Objects;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Entity
@Table(name = "PhoneNumbers")
public class PhoneNumber {
    @ToString.Include
    @JsonIgnore
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(nullable = false)
    private String phoneNumber;
    @Column(nullable = false)
    private String dialCode;
    @Column(nullable = false)
    private String isoCode;

    @JsonIgnore
    @OneToOne(cascade=CascadeType.MERGE)
    private Account account;

    public PhoneNumber(String phoneNumber, String dialCode, String isoCode) {
        this.phoneNumber = phoneNumber;
        this.dialCode = dialCode;
        this.isoCode = isoCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PhoneNumber that = (PhoneNumber) o;
        return phoneNumber.equals(that.phoneNumber) && dialCode.equals(that.dialCode) && isoCode.equals(that.isoCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(phoneNumber, dialCode, isoCode);
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
