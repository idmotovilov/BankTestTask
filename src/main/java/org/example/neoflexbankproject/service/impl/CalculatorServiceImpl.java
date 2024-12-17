package org.example.neoflexbankproject.service.impl;

import lombok.NonNull;
import lombok.Value;
import org.example.neoflexbankproject.dto.LoanOfferDto;
import org.example.neoflexbankproject.dto.LoanStatementRequestDto;
import org.example.neoflexbankproject.service.CalculatorService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class CalculatorServiceImpl implements CalculatorService {
    @Value("${loan.base-rate:15.0}")
    private double baseRate;
    private static final Pattern NAME_PATTERN = Pattern.compile("^[A-Za-z]{2,30}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$");
    private static final Pattern PASSPORT_SERIES_PATTERN = Pattern.compile("^\\d{4}$");
    private static final Pattern PASSPORT_NUMBER_PATTERN = Pattern.compile("^\\d{6}$");
    private static final int MIN_AMOUNT = 20000; // минимальная сумма
    private static final int MIN_TERM = 6;       // минимальный срок
    private static final int ADULT_AGE = 18;
    @Override
    public List<LoanOfferDto> salaryCalculation(@NonNull LoanStatementRequestDto loanStatementRequestDto) {
        List<LoanOfferDto> loanOfferDtos = new ArrayList<>();

        List<FlagCombination> combinations = List.of(
                new FlagCombination(false, false),
                new FlagCombination(false, true),
                new FlagCombination(true, false),
                new FlagCombination(true, true)
        );

       // return ;
    }
}
