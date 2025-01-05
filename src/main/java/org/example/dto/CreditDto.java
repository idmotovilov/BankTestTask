package org.example.dto;

import java.math.BigDecimal;

public record CreditDto(
        BigDecimal amount,
        Integer term,
        BigDecimal monthlyPayment,
        BigDecimal rate,
        BigDecimal psk,
        String paymentSchedule,  // строка, храним JSON
        boolean insuranceEnabled,
        boolean salaryClient
) {
}
