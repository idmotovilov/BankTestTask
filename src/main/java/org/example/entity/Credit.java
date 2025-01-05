package org.example.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.enums.CreditStatus;

import java.math.BigDecimal;

@Entity
@Table(name = "credit")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Credit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "credit_id")
    private Long creditId;

    private BigDecimal amount;
    private Integer term;
    private BigDecimal monthlyPayment;
    private BigDecimal rate;
    private BigDecimal psk;

    @Column(columnDefinition = "jsonb")
    private String paymentSchedule;

    private Boolean insuranceEnabled;
    private Boolean salaryClient;

    @Enumerated(EnumType.STRING)
    private CreditStatus creditStatus;
}
