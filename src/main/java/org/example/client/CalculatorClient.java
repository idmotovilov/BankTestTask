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
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
@Component
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
                    new org.springframework.core.ParameterizedTypeReference<>() {}
            );

            if (response == null) {
                throw new CalculatorClientException("Response is null", new NullPointerException("response = null"));
            }
            List<LoanOfferDto> offers = response.getBody();
            if (offers == null) {
                throw new CalculatorClientException("Response body is null", new NullPointerException("offers = null"));
            }

            log.info("Received offers: {}", offers);
            return offers;
        } catch (Exception e) {
            log.error("Error while fetching loan offers from calculator", e);
            // Ловим любое Exception (не только RestClientException),
            // чтобы гарантированно бросить CalculatorClientException
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
            CreditDto creditDto = restTemplate.postForObject(url, scoringData, CreditDto.class);
            if (creditDto == null) {
                throw new CalculatorClientException("CreditDto is null", new NullPointerException("creditDto = null"));
            }
            log.info("Received creditDto: {}", creditDto);
            return creditDto;
        } catch (Exception e) {
            log.error("Error while calculating credit from calculator", e);
            throw new CalculatorClientException("Failed to calculate credit", e);
        }
    }
}
