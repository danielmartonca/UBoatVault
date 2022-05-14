package com.example.uboatvault.api.model.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "AccountsDetails")
public class AccountDetails {
    @JsonIgnore
    @Id
    @Getter
    @Setter
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    private Long id;

    @Getter
    @Setter
    private String fullName;

    @Getter
    @Setter
    private String email;

    @Getter
    @Setter
    @Lob
    @Basic(fetch = FetchType.LAZY)
    private byte[] profileImage;

    //TODO add fields later if needed

    @JsonIgnore
    @Getter
    @Setter
    @OneToOne(mappedBy = "token", orphanRemoval = true)
    private Account account;
}
