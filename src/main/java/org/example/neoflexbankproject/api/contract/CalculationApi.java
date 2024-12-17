package org.example.neoflexbankproject.api.contract;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.neoflexbankproject.dto.CreditDto;
import org.example.neoflexbankproject.dto.LoanOfferDto;
import org.example.neoflexbankproject.dto.LoanStatementRequestDto;
import org.example.neoflexbankproject.dto.ScoringDataDto;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@RequestMapping("/calculator")
@Tag(name = "расчёт возможных условий кредита + скоринг данных", description = "расчёт условий кредита и скоринг")

public interface CalculationApi {

    @Operation(summary = "расчёт возможных условий кредита", description = "Request - LoanStatementRequestDto, response - List<LoanOfferDto>")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "запрос обработан успешно"),
            @ApiResponse(responseCode = "400", description = "запрос не прошёл валидацию"),
            @ApiResponse(responseCode = "500", description = "внутренняя ошибка сервиса")

    })
    @PostMapping("/offers")
    List<LoanOfferDto> offer(@Parameter(description = "LoanStatementRequestDto") @RequestBody LoanStatementRequestDto requestDto);

    @Operation(summary = "расчёт возможных условий кредита", description = "Request - ScoringDataDto, response - CreditDto")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "запрос обработан успешно"),
            @ApiResponse(responseCode = "400", description = "запрос не прошёл валидацию"),
            @ApiResponse(responseCode = "500", description = "внутренняя ошибка сервиса")

    })
@PostMapping("/calc")
    CreditDto calc(@Parameter(description = "ScoringDataDto") @RequestBody CreditDto requestDto);
