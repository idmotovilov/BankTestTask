package org.example.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.FinishRegistrationRequestDto;
import org.example.dto.LoanOfferDto;
import org.example.dto.LoanStatementRequestDto;
import org.example.service.DealService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/deal")
@RequiredArgsConstructor
public class DealController {

    private final DealService dealService;

    /**
     * Создание Statement и получение офферов (через шаблон execute).
     */
    @PostMapping("/statement")
    public List<LoanOfferDto> createStatement(@RequestBody LoanStatementRequestDto dto) {
        log.info("createStatement() called, dto={}", dto);
        // Вместо старого вызова createStatementAndGetOffers(dto)
        // используем метод execute(dto) из DealServiceTemplate
        return dealService.execute(dto);
    }

    /**
     * Выбор оффера.
     */
    @PostMapping("/offer/select")
    public void selectOffer(@RequestBody LoanOfferDto offer) {
        log.info("selectOffer() called, offer={}", offer);
        dealService.selectOffer(offer);
    }

    /**
     * Завершение регистрации + полный подсчёт кредита.
     */
    @PostMapping("/calculate/{statementId}")
    public void calculate(@PathVariable("statementId") Long statementId,
                          @RequestBody FinishRegistrationRequestDto dto) {
        log.info("calculate() called, statementId={}, dto={}", statementId, dto);
        dealService.finishRegistration(statementId, dto);
    }
}
