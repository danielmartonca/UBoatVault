package com.uboat.vault.api.model.domain.sailing;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Entity
@Table(name = "JourneysErrorRecords")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JourneyError {
    @JsonIgnore
    @Id
    @Getter
    @Setter
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    private Long id;

    @NotNull
    @JsonIgnore
    @Getter
    @Setter
    @OneToOne()
    @JoinColumn(name = "journey_id")
    private Journey journey;

    @NotNull
    @NotEmpty
    @Getter
    @Setter
    private String reason;

    @NotNull
    @Getter
    @Setter
    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date dateRecorded = new Date();
}