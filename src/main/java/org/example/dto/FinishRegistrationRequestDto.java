package org.example.dto;

import java.math.BigDecimal;

public record FinishRegistrationRequestDto(
        String gender,
        String maritalStatus,
        Integer dependentAmount,
        String employerInn,
        BigDecimal salary,
        String position,
        Integer workExperienceTotal,
        Integer workExperienceCurrent
) {
}
