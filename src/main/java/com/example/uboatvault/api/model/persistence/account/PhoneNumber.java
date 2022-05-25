package com.example.uboatvault.api.model.persistence.account;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;
import java.util.Objects;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Entity
@Table(name = "PhoneNumbers")
public class PhoneNumber {
    @JsonIgnore
    @Id
    @Getter
    @Setter
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    private Long id;

    @Getter
    @Setter
    @Column(nullable = false)
    private String phoneNumber;
    @Getter
    @Setter
    @Column(nullable = false)
    private String dialCode;
    @Getter
    @Setter
    @Column(nullable = false)
    private String isoCode;

    @JsonIgnore
    @Getter
    @Setter
    @OneToOne(mappedBy = "phoneNumber", cascade = CascadeType.MERGE)
    private Account account;

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
