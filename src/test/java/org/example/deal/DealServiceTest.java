package org.example.deal;

import org.example.client.CalculatorClient;
import org.example.dto.*;
import org.example.entity.*;
import org.example.enums.*;
import org.example.exception.DealServiceException;
import org.example.mapper.ClientMapper;
import org.example.repository.*;
import org.example.service.DealService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class DealServiceTest {

    @Mock private ClientRepository clientRepository;
    @Mock private StatementRepository statementRepository;
    @Mock private CreditRepository creditRepository;
    @Mock private PassportRepository passportRepository;
    @Mock private EmploymentRepository employmentRepository;
    @Mock private CalculatorClient calculatorClient;
    @Mock private ClientMapper clientMapper;

    @InjectMocks private DealService dealService;

    @Captor private ArgumentCaptor<Client> clientCaptor;
    @Captor private ArgumentCaptor<Statement> statementCaptor;

    private LoanStatementRequestDto requestDto;
    private Client client;
    private Statement statement;
    private Credit credit;

    @BeforeEach
    void setUp() {
        requestDto = new LoanStatementRequestDto(
                new BigDecimal("10000"), 12, "John", "Doe", "Middle",
                LocalDate.of(1990, 1, 1), "john@example.com", "1234", "567890"
        );
        client = new Client();
        statement = new Statement();
        statement.setClient(client);
        statement.setStatementId(1L);
        credit = new Credit();
    }

    @Test
    void process_ShouldCreateClientAndStatement() {
        lenient().when(clientMapper.toEntity(any(LoanStatementRequestDto.class))).thenReturn(client);
        lenient().when(clientRepository.save(any(Client.class))).thenReturn(client);
        lenient().when(statementRepository.save(any(Statement.class))).thenReturn(statement);

        when(calculatorClient.getLoanOffers(any(ScoringDataDto.class))).thenReturn(List.of(
                new LoanOfferDto(1L, new BigDecimal("10000"), 12, new BigDecimal("500"), new BigDecimal("5"), true, true)
        ));

        List<LoanOfferDto> offers = dealService.process(requestDto);

        assertNotNull(offers);
        assertFalse(offers.isEmpty());
        verify(clientRepository).save(clientCaptor.capture());
        verify(statementRepository).save(statementCaptor.capture());

        Client capturedClient = clientCaptor.getValue();
        Statement capturedStatement = statementCaptor.getValue();
        assertNotNull(capturedClient);
        assertNotNull(capturedStatement);
    }




    @Test
    void selectOffer_ShouldUpdateStatementStatus() {
        LoanOfferDto offerDto = new LoanOfferDto(1L, new BigDecimal("10000"), 12, new BigDecimal("500"), new BigDecimal("5"), true, true);
        when(statementRepository.findById(1L)).thenReturn(Optional.of(statement));

        dealService.selectOffer(offerDto);

        verify(statementRepository).save(statementCaptor.capture());
        Statement updatedStatement = statementCaptor.getValue();
        assertEquals(ApplicationStatus.APPROVED, updatedStatement.getStatus());
    }

    @Test
    void finishRegistration_ShouldUpdateClientAndCredit() {
        FinishRegistrationRequestDto finishDto = new FinishRegistrationRequestDto(
                "male", "married", 2, "123456789", new BigDecimal("5000"), "MID_MANAGER", 5, 3
        );
        when(statementRepository.findById(1L)).thenReturn(Optional.of(statement));
        // Подготовка возвращаемого значения creditDto
        CreditDto creditDto = new CreditDto(
                new BigDecimal("10000"), 12, new BigDecimal("300"), new BigDecimal("5"), new BigDecimal("0.05"),
                "schedule", true, true
        );
        when(calculatorClient.calculateCredit(any(ScoringDataDto.class))).thenReturn(creditDto);

        dealService.finishRegistration(1L, finishDto);

        // Проверяем оба вызова clientRepository.save
        verify(clientRepository, times(2)).save(clientCaptor.capture());
        verify(creditRepository).save(any(Credit.class));
        Client updatedClient = clientCaptor.getAllValues().get(0); // Первый вызов
        assertNotNull(updatedClient.getGender());
        assertNotNull(updatedClient.getMaritalStatus());
    }


    @Test
    void finishRegistration_ShouldThrowExceptionWhenStatementNotFound() {
        when(statementRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(DealServiceException.class, () -> dealService.finishRegistration(1L, new FinishRegistrationRequestDto("male", "married", 2, "123456789", new BigDecimal("5000"), "MID_MANAGER", 5, 3)));
    }
}
