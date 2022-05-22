package com.example.uboatvault.api.model.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@AllArgsConstructor
@NoArgsConstructor
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


    @JsonIgnore
    @Getter
    @Setter
    @OneToOne(mappedBy = "accountDetails", orphanRemoval = true)
    private Account account;


    @Getter
    @Setter
    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE})
    @JoinColumn(name = "image_id")
    private Image image;

    public AccountDetails(AccountDetails accountDetails) {
        this.fullName = accountDetails.fullName;
        this.email = accountDetails.email;
        this.image = accountDetails.image;
    }
}
