package org.example.deal;

import org.example.client.CalculatorClient;
import org.example.dto.CreditDto;
import org.example.dto.LoanOfferDto;
import org.example.dto.ScoringDataDto;
import org.example.exception.CalculatorClientException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Тестируем CalculatorClient с помощью Mockito.
 * Демонстрируем как "положительные" (ok) тесты, так и тесты на исключения.
 */
class CalculatorClientTest {

    private CalculatorClient calculatorClient;
    private RestTemplate restTemplate; // «мок» RestTemplate

    @BeforeEach
    void setUp() {
        // 1. Создаём «мок» (подделку) RestTemplate
        restTemplate = mock(RestTemplate.class);
        // 2. Создаём CalculatorClient, который обычно сам создаёт RestTemplate
        calculatorClient = new CalculatorClient("http://localhost:8081");
        // 3. Через ReflectionTestUtils подменяем внутреннее поле "restTemplate" на наш мок
        ReflectionTestUtils.setField(calculatorClient, "restTemplate", restTemplate);
    }

    // ======================= getLoanOffers() =======================

    @Test
    void getLoanOffers_ok() {
        // given
        ScoringDataDto scoringData = new ScoringDataDto(
                BigDecimal.valueOf(100000), 12,
                "John", "Doe", null, null, null,
                null, null, null, null, null,
                null, null, false, false
        );

        // Формируем фейковое "успешное" предложение
        LoanOfferDto mockOffer = new LoanOfferDto(
                1L, BigDecimal.valueOf(100000), 12,
                null, null, false, false
        );
        List<LoanOfferDto> mockOffers = Collections.singletonList(mockOffer);

        // Создаём "ответ" сервиса в виде ResponseEntity с кодом 200 OK
        ResponseEntity<List<LoanOfferDto>> mockResponse =
                new ResponseEntity<>(mockOffers, HttpStatus.OK);

        // Настраиваем restTemplate.exchange(...) возвращать mockResponse
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                ArgumentMatchers.<ParameterizedTypeReference<List<LoanOfferDto>>>any()
        )).thenReturn(mockResponse);

        // when
        List<LoanOfferDto> result = calculatorClient.getLoanOffers(scoringData);

        // then
        // Убеждаемся, что результат не null и содержит наш mockOffer
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(BigDecimal.valueOf(100000), result.get(0).requestedAmount());

        // Проверяем, что restTemplate.exchange(...) вызван 1 раз
        verify(restTemplate, times(1)).exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                ArgumentMatchers.<ParameterizedTypeReference<List<LoanOfferDto>>>any()
        );
    }

    @Test
    void getLoanOffers_exception() {
        // given
        ScoringDataDto scoringData = new ScoringDataDto(
                null,null,null,null,null,
                null,null,null,null,null,
                null,null,null,null,false,false
        );

        // Эмулируем любую RuntimeException
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                ArgumentMatchers.<ParameterizedTypeReference<List<LoanOfferDto>>>any()
        )).thenThrow(new RuntimeException("Simulated error"));

        // when & then
        // Ожидаем, что при возникновении "Simulated error"
        //  CalculatorClient обернёт это в CalculatorClientException
        assertThrows(CalculatorClientException.class, () -> {
            calculatorClient.getLoanOffers(scoringData);
        });
    }

    // ======================= calculateCredit() =======================

    @Test
    void calculateCredit_ok() {
        // given
        ScoringDataDto scoringData = new ScoringDataDto(
                null,null,null,null,null,null,
                null,null,null,null,null,null,
                null,null,false,false
        );

        // Допустим, калькулятор вернёт CreditDto
        CreditDto mockCredit = new CreditDto(
                BigDecimal.valueOf(50000), 10,
                null, null, null,
                null, false, false
        );

        // Настраиваем postForObject(...) возвращать mockCredit
        when(restTemplate.postForObject(
                anyString(), any(), eq(CreditDto.class))
        ).thenReturn(mockCredit);

        // when
        CreditDto result = calculatorClient.calculateCredit(scoringData);

        // then
        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(50000), result.amount());
        verify(restTemplate, times(1))
                .postForObject(anyString(), any(), eq(CreditDto.class));
    }

    @Test
    void calculateCredit_exception() {
        // given
        // эмулируем RuntimeException
        when(restTemplate.postForObject(anyString(), any(), eq(CreditDto.class)))
                .thenThrow(new RuntimeException("Simulated error"));

        // when & then
        assertThrows(CalculatorClientException.class, () -> {
            calculatorClient.calculateCredit(
                    new ScoringDataDto(null,null,null,null,null,null,
                            null,null,null,null,null,null,null,null,false,false)
            );
        });
    }
}
