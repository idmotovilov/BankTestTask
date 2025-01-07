package org.example.deal;

import org.example.controller.DealController;
import org.example.dto.FinishRegistrationRequestDto;
import org.example.dto.LoanOfferDto;
import org.example.dto.LoanStatementRequestDto;
import org.example.service.DealService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@WebMvcTest(DealController.class)
class DealControllerTest {

    @Autowired
    private MockMvc mockMvc;


    private DealService dealService; // мокируем сервис

    @Test
    @DisplayName("POST /deal/statement -> success")
    void testCreateStatement_success() throws Exception {
        // given
        when(dealService.createStatementAndGetOffers(any(LoanStatementRequestDto.class)))
                .thenReturn(List.of(
                        new LoanOfferDto(1L, BigDecimal.valueOf(100000), 12,
                                null, null, false, false)
                ));

        // when & then
        String requestBody = """
                {
                  "amount": 100000,
                  "term": 12,
                  "firstName": "John",
                  "lastName": "Doe"
                }
                """;

        mockMvc.perform(post("/deal/statement")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk());

        // проверяем, что сервис вызвался
        verify(dealService, times(1))
                .createStatementAndGetOffers(any(LoanStatementRequestDto.class));
    }

    @Test
    @DisplayName("POST /deal/statement -> exception")
    void testCreateStatement_exception() throws Exception {
        // given
        when(dealService.createStatementAndGetOffers(any(LoanStatementRequestDto.class)))
                .thenThrow(new RuntimeException("Simulated error"));

        // when & then
        String requestBody = """
                {
                  "amount": 50000,
                  "term": 6
                }
                """;

        mockMvc.perform(post("/deal/statement")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isInternalServerError()); // т.к. не перехватываем глобально, даёт 500

        verify(dealService, times(1))
                .createStatementAndGetOffers(any(LoanStatementRequestDto.class));
    }

    @Test
    @DisplayName("POST /deal/offer/select -> 200")
    void testSelectOffer_success() throws Exception {
        String requestBody = """
                {
                  "statementId": 10,
                  "requestedAmount": 100000
                }
                """;
        mockMvc.perform(post("/deal/offer/select")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk());

        verify(dealService, times(1))
                .selectOffer(any(LoanOfferDto.class));
    }

    @Test
    @DisplayName("POST /deal/calculate/5 -> 200")
    void testCalculate_success() throws Exception {
        String requestBody = """
                {
                  "gender": "MALE"
                }
                """;

        mockMvc.perform(post("/deal/calculate/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk());

        verify(dealService, times(1))
                .finishRegistration(eq(5L), any(FinishRegistrationRequestDto.class));
    }
}
