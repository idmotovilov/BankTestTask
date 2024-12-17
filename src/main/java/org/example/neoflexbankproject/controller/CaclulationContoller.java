package org.example.neoflexbankproject.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.neoflexbankproject.api.contract.CalculationApi;
import org.example.neoflexbankproject.dto.CreditDto;
import org.example.neoflexbankproject.dto.LoanOfferDto;
import org.example.neoflexbankproject.dto.LoanStatementRequestDto;
import org.example.neoflexbankproject.dto.ScoringDataDto;
import org.example.neoflexbankproject.service.CalculatorService;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor

public class CaclulationContoller implements CalculationApi {
    private final CalculatorService calculatorService;
    @Override
    public List<LoanOfferDto> offer(LoanStatementRequestDto requestDto) {
        return calculatorService.salaryCalculation(requestDto);
    }

    @Override
    public CreditDto calc(ScoringDataDto scoringDataDto) {
        return null;
    }
}
