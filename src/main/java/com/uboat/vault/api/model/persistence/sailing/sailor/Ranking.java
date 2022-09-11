package com.uboat.vault.api.model.persistence.sailing.sailor;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "Rankings")
public class Ranking {
    @Id
    @Getter
    @Setter
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    private Long id;

    @Getter
    @Setter
    private Long clientId;

    @Getter
    @Setter
    private double evaluation;

    @Getter
    @Setter
    @Column(name = "evaluation_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date evaluationDate;

    @JsonIgnore
    @Getter
    @Setter
    @ManyToOne()
    @JoinColumn(name = "active_sailor_id", nullable = false)
    private ActiveSailor activeSailor;
}

