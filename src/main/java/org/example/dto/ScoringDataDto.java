package org.example.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ScoringDataDto(
        BigDecimal amount,
        Integer term,
        String firstName,
        String lastName,
        String middleName,
        LocalDate birthDate,
        String gender,
        String maritalStatus,
        Integer dependentAmount,

        String employerInn,
        BigDecimal salary,
        String position,
        Integer workExperienceTotal,
        Integer workExperienceCurrent,

        boolean insuranceEnabled,
        boolean salaryClient
) {
}
