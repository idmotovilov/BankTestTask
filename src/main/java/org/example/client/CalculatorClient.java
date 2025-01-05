package org.example.client;


import org.example.dto.CreditDto;
import org.example.dto.LoanOfferDto;
import org.example.dto.ScoringDataDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
@Configuration
public class CalculatorClient {

    private final RestTemplate restTemplate;
    private final String calculatorBaseUrl;

    public CalculatorClient(@Value("${calculator.url}") String calculatorBaseUrl) {
        this.restTemplate = new RestTemplate();
        this.calculatorBaseUrl = calculatorBaseUrl;
    }

    public List<LoanOfferDto> getLoanOffers(ScoringDataDto scoringData) {
        String url = calculatorBaseUrl + "/calculator/offers";
        ResponseEntity<List<LoanOfferDto>> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                new HttpEntity<>(scoringData),
                new org.springframework.core.ParameterizedTypeReference<>() {}
        );
        return response.getBody();
    }

    public CreditDto calculateCredit(ScoringDataDto scoringData) {
        String url = calculatorBaseUrl + "/calculator/calc";
        return restTemplate.postForObject(url, scoringData, CreditDto.class);
    }
}
