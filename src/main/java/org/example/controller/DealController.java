package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.dto.FinishRegistrationRequestDto;
import org.example.dto.LoanOfferDto;
import org.example.dto.LoanStatementRequestDto;
import org.example.service.DealService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/deal")
@RequiredArgsConstructor
public class DealController {

    private final DealService dealService;

    @PostMapping("/statement")
    public List<LoanOfferDto> createStatement(@RequestBody LoanStatementRequestDto dto) {
        return dealService.createStatementAndGetOffers(dto);
    }

    @PostMapping("/offer/select")
    public void selectOffer(@RequestBody LoanOfferDto offer) {
        dealService.selectOffer(offer);
    }

    @PostMapping("/calculate/{statementId}")
    public void calculate(@PathVariable("statementId") Long statementId,
                          @RequestBody FinishRegistrationRequestDto dto) {
        dealService.finishRegistration(statementId, dto);
    }
}
