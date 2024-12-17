package org.example.neoflexbankproject.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LoanStatementRequestDto(
        @NotNull(message = "Сумма кредита - действительно число не может быть null")
        @DecimalMin(value = "20000.0", inclusive = false, message = "Сумма кредита - действительно число, большее или равное 20000.")

        BigDecimal amount,

        Integer term,
        @NotNull
        String firstName,
        @NotNull
        String lastName,
        String middleName,
        @NotNull
        String email,
        @NotNull
        LocalDate birthdate,
        @NotNull
        String passportSeries,
        @NotNull
        String passportNumber
) {}
