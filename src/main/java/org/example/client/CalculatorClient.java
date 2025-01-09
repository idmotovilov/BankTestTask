package org.example.client;

import lombok.extern.slf4j.Slf4j;
import org.example.dto.CreditDto;
import org.example.dto.LoanOfferDto;
import org.example.dto.ScoringDataDto;
import org.example.exception.CalculatorClientException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.ParameterizedTypeReference;

import java.util.List;

@Slf4j

@Configuration
public class CalculatorClient {

    private final RestTemplate restTemplate;
    private final String calculatorBaseUrl;

    public CalculatorClient(@Value("${calculator.url}") String calculatorBaseUrl) {
        this.restTemplate = new RestTemplate();
        this.calculatorBaseUrl = calculatorBaseUrl;
    }

    /**
     * Запрос на /calculator/offers, возвращает список LoanOfferDto
     */
    public List<LoanOfferDto> getLoanOffers(ScoringDataDto scoringData) {
        log.info("getLoanOffers() called with scoringData={}", scoringData);
        String url = calculatorBaseUrl + "/calculator/offers";
        try {
            ResponseEntity<List<LoanOfferDto>> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    new HttpEntity<>(scoringData),
                    new ParameterizedTypeReference<>() {}
            );

            if (response == null || response.getBody() == null) {
                throw new CalculatorClientException("Response body is null",
                        new NullPointerException("offers = null"));
            }
            List<LoanOfferDto> offers = response.getBody();
            log.info("Received offers: {}", offers);
            return offers;
        } catch (Exception e) {
            log.error("Error while fetching loan offers from calculator", e);
            throw new CalculatorClientException("Failed to get loan offers", e);
        }
    }

    /**
     * Запрос на /calculator/calc, возвращает один CreditDto
     */
    public CreditDto calculateCredit(ScoringDataDto scoringData) {
        log.info("calculateCredit() called with scoringData={}", scoringData);
        String url = calculatorBaseUrl + "/calculator/calc";
        try {
            ResponseEntity<CreditDto> response = restTemplate.postForEntity(
                    url,
                    new HttpEntity<>(scoringData),
                    CreditDto.class
            );
            if (response.getBody() == null) {
                throw new CalculatorClientException("CreditDto is null",
                        new NullPointerException("creditDto = null"));
            }
            CreditDto creditDto = response.getBody();
            log.info("Received creditDto: {}", creditDto);
            return creditDto;
        } catch (Exception e) {
            log.error("Error while calculating credit from calculator", e);
            throw new CalculatorClientException("Failed to calculate credit", e);
        }
    }
}
