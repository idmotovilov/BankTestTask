package org.example.dto;

import java.math.BigDecimal;

public record LoanOfferDto(
        Long statementId,
        BigDecimal requestedAmount,
        Integer term,
        BigDecimal monthlyPayment,
        BigDecimal rate,
        boolean insuranceEnabled,
        boolean salaryClient
) {
}
