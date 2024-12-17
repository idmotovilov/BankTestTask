package org.example.neoflexbankproject.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public record ScoringDataDto(
        @NotNull(message = "Сумма кредита - действительно число не может быть null")
        @DecimalMin(value = "20000.0", inclusive = false, message = "Сумма кредита - действительно число, большее или равное 20000.")

        @NonNull
        BigDecimal amount,
        Integer term,
        @NonNull
        String firstName,
        @NonNull
        String lastName,
        String middleName,
        Enum gender,
        @NonNull// Замените Enum на конкретный enum, например Gender
        LocalDate birthdate,
        @NonNull
        String passportSeries,
        @NonNull
        String passportNumber,
        @NonNull
        LocalDate passportIssueDate,
        String passportIssueBranch,
        Enum maritalStatus,        // Замените Enum на конкретный enum, например MaritalStatus
        Integer dependentAmount,
        EmploymentDto employment,
        @NonNull
        String accountNumber,
        Boolean isInsuranceEnabled,
        Boolean isSalaryClient
) {}