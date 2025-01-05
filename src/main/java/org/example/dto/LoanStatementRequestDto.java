package org.example.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LoanStatementRequestDto(
        BigDecimal amount,
        Integer term,
        String firstName,
        String lastName,
        String middleName,
        LocalDate birthDate,
        String email,
        String passportSeries,
        String passportNumber
) {
}
