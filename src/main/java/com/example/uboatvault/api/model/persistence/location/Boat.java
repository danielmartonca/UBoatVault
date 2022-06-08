package com.example.uboatvault.api.model.persistence.location;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@NoArgsConstructor
@Entity
@Table(name = "Boats")
public class Boat {
    @Id
    @Getter
    @Setter
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    private Long id;

    @Getter
    @Setter
    private double averageSpeed;

    @Getter
    @Setter
    @OneToOne(mappedBy = "boat", cascade = CascadeType.MERGE)
    private ActiveSailor sailor;
}
