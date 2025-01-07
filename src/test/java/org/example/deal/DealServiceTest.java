package org.example.deal;

import org.example.client.CalculatorClient;
import org.example.dto.*;
import org.example.entity.*;
import org.example.enums.ApplicationStatus;
import org.example.exception.DealServiceException;
import org.example.repository.*;
import org.example.service.DealService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DealServiceTest {

    @Mock
    private ClientRepository clientRepository;
    @Mock
    private StatementRepository statementRepository;
    @Mock
    private CreditRepository creditRepository;
    @Mock
    private PassportRepository passportRepository;
    @Mock
    private EmploymentRepository employmentRepository;
    @Mock
    private CalculatorClient calculatorClient;

    @InjectMocks
    private DealService dealService; // тут Spring не нужен, просто junit+mockito

    @BeforeEach
    void setUp() {
    }

    @Test
    void createStatementAndGetOffers_success() {
        // given
        LoanStatementRequestDto dto = new LoanStatementRequestDto(
                BigDecimal.valueOf(100000),
                12,
                "John", "Doe", null,
                LocalDate.of(1990, 1, 1),
                null,
                null,
                null
        );

        Client savedClient = new Client();
        savedClient.setClientId(1L);

        Statement savedStatement = new Statement();
        savedStatement.setStatementId(1L);

        when(clientRepository.save(any(Client.class))).thenReturn(savedClient);
        when(statementRepository.save(any(Statement.class))).thenReturn(savedStatement);

        // Мокаем ответ из CalculatorClient
        LoanOfferDto offer = new LoanOfferDto(1L, BigDecimal.valueOf(100000), 12,
                null, null, false, false);
        when(calculatorClient.getLoanOffers(any(ScoringDataDto.class)))
                .thenReturn(List.of(offer));

        // when
        List<LoanOfferDto> offers = dealService.createStatementAndGetOffers(dto);

        // then
        assertNotNull(offers);
        assertEquals(1, offers.size());
        assertEquals(BigDecimal.valueOf(100000), offers.get(0).requestedAmount());

        // Проверим, что методы репозиториев вызывались
        verify(clientRepository, times(1)).save(any(Client.class));
        verify(statementRepository, times(1)).save(any(Statement.class));
        verify(calculatorClient, times(1)).getLoanOffers(any(ScoringDataDto.class));
    }

    @Test
    void createStatementAndGetOffers_exceptionInClientRepository() {
        // given
        LoanStatementRequestDto dto = new LoanStatementRequestDto(
                BigDecimal.valueOf(50000), 12,
                "Jane", "Doe", null,
                null, null, null, null
        );

        when(clientRepository.save(any(Client.class)))
                .thenThrow(new RuntimeException("DB error"));

        // when & then
        // DealService оборачивает любые ошибки в DealServiceException
        assertThrows(DealServiceException.class, () -> {
            dealService.createStatementAndGetOffers(dto);
        });

        verify(clientRepository, times(1)).save(any(Client.class));
        verifyNoMoreInteractions(statementRepository); // т.к. упало ещё на сохранении клиента
    }

    @Test
    void selectOffer_success() {
        // given
        LoanOfferDto offer = new LoanOfferDto(2L, BigDecimal.valueOf(50000), 24,
                null, null, false, false);

        Statement statement = new Statement();
        statement.setStatementId(2L);
        statement.setStatus(ApplicationStatus.PREAPPROVAL);

        when(statementRepository.findById(2L)).thenReturn(Optional.of(statement));

        // when
        dealService.selectOffer(offer);

        // then
        assertEquals(ApplicationStatus.APPROVED, statement.getStatus());
        verify(statementRepository, times(1)).save(statement);
    }

    @Test
    void selectOffer_statementNotFound() {
        // given
        LoanOfferDto offer = new LoanOfferDto(999L, BigDecimal.valueOf(50000), 24,
                null, null, false, false);
        when(statementRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThrows(DealServiceException.class, () -> {
            dealService.selectOffer(offer);
        });
        verify(statementRepository, never()).save(any(Statement.class));
    }

    @Test
    void finishRegistration_success() {
        // given
        Long statementId = 100L;
        FinishRegistrationRequestDto dto = new FinishRegistrationRequestDto(
                "MALE", "SINGLE", 1, "1234567890", BigDecimal.valueOf(50000),
                "WORKER", 10, 5
        );

        Statement statement = new Statement();
        statement.setStatementId(statementId);

        Client client = new Client();
        client.setClientId(1L);

        statement.setClient(client);

        when(statementRepository.findById(statementId)).thenReturn(Optional.of(statement));

        CreditDto mockCredit = new CreditDto(
                BigDecimal.valueOf(200000), 24,
                BigDecimal.valueOf(10000), BigDecimal.valueOf(14.0),
                BigDecimal.valueOf(16.5),
                null,
                false,
                false
        );
        when(calculatorClient.calculateCredit(any(ScoringDataDto.class))).thenReturn(mockCredit);

        // when
        dealService.finishRegistration(statementId, dto);

        // then
        verify(statementRepository, times(1)).findById(statementId);
        verify(employmentRepository, times(1)).save(any(Employment.class));
        verify(creditRepository, times(1)).save(any(Credit.class));
        verify(statementRepository, times(1)).save(statement);
    }
}
