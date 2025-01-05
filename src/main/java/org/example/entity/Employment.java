package org.example.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.enums.EmploymentPosition;
import org.example.enums.EmploymentStatus;

import java.math.BigDecimal;

@Entity
@Table(name = "employment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Employment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "employment_id")
    private Long employmentId;

    @Enumerated(EnumType.STRING)
    private EmploymentStatus status;

    private String employerInn;
    private BigDecimal salary;

    @Enumerated(EnumType.STRING)
    private EmploymentPosition position;

    private Integer workExperienceTotal;
    private Integer workExperienceCurrent;
}
