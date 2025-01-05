package org.example.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.enums.ApplicationStatus;

import java.time.LocalDate;

@Entity
@Table(name = "statement")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Statement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "statement_id")
    private Long statementId;

    @ManyToOne
    @JoinColumn(name = "client_id")
    private Client client;

    @OneToOne
    @JoinColumn(name = "credit_id")
    private Credit credit;

    @Enumerated(EnumType.STRING)
    private ApplicationStatus status;

    private LocalDate creationDate;

    // храним JSON как String (например, в колонке формата jsonb)
    @Column(columnDefinition = "jsonb")
    private String appliedOffer;

    @Column(columnDefinition = "jsonb")
    private String statusHistory;

    private LocalDate signDate;
    private String sesCode;
}
