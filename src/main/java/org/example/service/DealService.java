package org.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.client.CalculatorClient;
import org.example.dto.*;
import org.example.entity.*;
import org.example.enums.*;
import org.example.exception.DealServiceException;
import org.example.mapper.ClientMapper;
import org.example.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Slf4j
@Service
@RequiredArgsConstructor
public class DealService extends DealServiceTemplate<LoanStatementRequestDto, List<LoanOfferDto>> {

    private final ClientRepository clientRepository;
    private final StatementRepository statementRepository;
    private final CreditRepository creditRepository;
    private final PassportRepository passportRepository;
    private final EmploymentRepository employmentRepository;
    private final CalculatorClient calculatorClient;



    // =========================================================================
    // Реализация шаблонных методов для операции createStatementAndGetOffers
    // =========================================================================


    @Override
    protected void validate(LoanStatementRequestDto request) {
        log.debug("Validating request: {}", request);
        // Если нужна дополнительная валидация, можно её добавить
    }


    @Override
    public List<LoanOfferDto> process(LoanStatementRequestDto dto) {
        log.info("Processing createStatementAndGetOffers for DTO: {}", dto);

        Client client = createAndSaveClient(dto);


        Statement statement = createAndSaveStatement(client);

        // 3) Вызываем калькулятор и формируем список офферов
        ScoringDataDto scoringData = buildScoringData(dto);
        List<LoanOfferDto> offers = fetchOffers(scoringData);
        return mapOffersWithStatementId(offers, statement.getStatementId());
    }

    // -------------------------------------------------------------------------
    // Публичные методы selectOffer и finishRegistration
    // -------------------------------------------------------------------------


    /**
     * Выбор оффера.
     * Изменяем сущность Statement => требуется @Transactional.
     */
    @Transactional
    public void selectOffer(LoanOfferDto offer) {
        log.info("selectOffer called with offer: {}", offer);
        try {
            Statement statement = getStatementById(offer.statementId());
            applyOffer(statement, offer);
        } catch (Exception e) {
            log.error("Error in selectOffer", e);
            throw new DealServiceException("Failed to select offer for statementId=" + offer.statementId(), e);
        }
    }

    /**
     * Завершение регистрации + полный подсчёт кредита.
     * Тоже изменяем состояние БД => @Transactional.
     */
    @Transactional
    public void finishRegistration(Long statementId, FinishRegistrationRequestDto dto) {
        log.info("finishRegistration called with statementId={}, dto={}", statementId, dto);
        try {
            Statement statement = getStatementById(statementId);
            Client client = statement.getClient();

            updateClientFields(client, dto);
            Employment employment = updateEmployment(client, dto);


            log.info("Statement has appliedOffer: {}", statement.getAppliedOffer());


            CreditDto creditDto = calculateCredit(client, employment);
            saveCreditAndUpdateStatement(statement, creditDto);

            log.info("finishRegistration completed successfully");
        } catch (Exception e) {
            log.error("Error in finishRegistration", e);
            throw new DealServiceException("Failed to finish registration for statementId=" + statementId, e);
        }
    }

    // =========================================================================
    // ЧАСТЬ 1: Вспомогательные методы для process(...)
    // =========================================================================

    private Client createAndSaveClient(LoanStatementRequestDto dto) {
        Client client = ClientMapper.INSTANCE.toEntity(dto);

        // Сохраняем паспорт, если есть данные
        if (dto.passportSeries() != null && dto.passportNumber() != null) {
            Passport passport = new Passport();
            passport.setSeries(dto.passportSeries());
            passport.setNumber(dto.passportNumber());
            passportRepository.save(passport);
            client.setPassport(passport);
            log.debug("Saved passport: {}", passport);
        }

        clientRepository.save(client);
        log.debug("Saved Client: {}", client);
        return client;
    }

    private Statement createAndSaveStatement(Client client) {
        Statement statement = new Statement();
        statement.setClient(client);
        statement.setStatus(ApplicationStatus.PREAPPROVAL);
        statement.setCreationDate(LocalDate.now());
        statementRepository.save(statement);
        log.debug("Saved Statement: {}", statement);
        return statement;
    }

    private ScoringDataDto buildScoringData(LoanStatementRequestDto dto) {
        return new ScoringDataDto(
                dto.amount(),
                dto.term(),
                dto.firstName(),
                dto.lastName(),
                dto.middleName(),
                dto.birthDate(),
                null, // gender
                null, // maritalStatus
                null, // dependentAmount
                null, // employerInn
                null, // salary
                null, // position
                null, // workExperienceTotal
                null, // workExperienceCurrent
                false,
                false
        );
    }

    private List<LoanOfferDto> fetchOffers(ScoringDataDto scoringData) {
        List<LoanOfferDto> offers = calculatorClient.getLoanOffers(scoringData);
        if (offers == null || offers.isEmpty()) {
            log.warn("No loan offers received for scoring data: {}", scoringData);
        } else {
            log.info("Received offers from calculator: {}", offers);
        }
        return offers;
    }

    private List<LoanOfferDto> mapOffersWithStatementId(List<LoanOfferDto> offers, Long statementId) {
        // Применяем Stream API
        return offers.stream()
                .map(offer -> new LoanOfferDto(
                        statementId,
                        offer.requestedAmount(),
                        offer.term(),
                        offer.monthlyPayment(),
                        offer.rate(),
                        offer.insuranceEnabled(),
                        offer.salaryClient()
                ))
                .collect(toList());
    }

    // =========================================================================
    // ЧАСТЬ 2: Вспомогательные методы для selectOffer(...)
    // =========================================================================

    private Statement getStatementById(Long statementId) {
        return statementRepository.findById(statementId)
                .orElseThrow(() -> new DealServiceException("Statement not found. Id=" + statementId));
    }

    private void applyOffer(Statement statement, LoanOfferDto offer) {
        statement.setStatus(ApplicationStatus.APPROVED);
        statement.setAppliedOffer(offer.toString());
        statementRepository.save(statement);
        log.debug("Offer applied to statement: {}", statement);
    }

    // =========================================================================
    // ЧАСТЬ 3: Вспомогательные методы для finishRegistration(...)
    // =========================================================================

    private void updateClientFields(Client client, FinishRegistrationRequestDto dto) {
        if (dto.gender() != null) {
            client.setGender(Gender.valueOf(dto.gender().toUpperCase()));
        }
        if (dto.maritalStatus() != null) {
            client.setMaritalStatus(MaritalStatus.valueOf(dto.maritalStatus().toUpperCase()));
        }
        if (dto.dependentAmount() != null) {
            client.setDependentAmount(dto.dependentAmount());
        }
        clientRepository.save(client);
        log.debug("Updated client fields: {}", client);
    }

    private Employment updateEmployment(Client client, FinishRegistrationRequestDto dto) {
        Employment employment = client.getEmployment();
        if (employment == null) {
            employment = new Employment();
        }
        if (dto.employerInn() != null) {
            employment.setEmployerInn(dto.employerInn());
        }
        if (dto.salary() != null) {
            employment.setSalary(dto.salary());
        }
        if (dto.position() != null) {
            employment.setPosition(EmploymentPosition.valueOf(dto.position().toUpperCase()));
        }
        if (dto.workExperienceTotal() != null) {
            employment.setWorkExperienceTotal(dto.workExperienceTotal());
        }
        if (dto.workExperienceCurrent() != null) {
            employment.setWorkExperienceCurrent(dto.workExperienceCurrent());
        }

        employmentRepository.save(employment);
        client.setEmployment(employment);
        clientRepository.save(client);

        log.debug("Updated employment: {}", employment);
        return employment;
    }

    private CreditDto calculateCredit(Client client, Employment employment) {
        ScoringDataDto scoringData = new ScoringDataDto(
                null,
                null,
                client.getFirstName(),
                client.getLastName(),
                client.getMiddleName(),
                client.getBirthDate(),
                client.getGender() != null ? client.getGender().name() : null,
                client.getMaritalStatus() != null ? client.getMaritalStatus().name() : null,
                client.getDependentAmount(),
                employment.getEmployerInn(),
                employment.getSalary(),
                employment.getPosition() != null ? employment.getPosition().name() : null,
                employment.getWorkExperienceTotal(),
                employment.getWorkExperienceCurrent(),
                false,
                false
        );
        log.info("Requesting credit calculation with scoringData: {}", scoringData);

        CreditDto creditDto = calculatorClient.calculateCredit(scoringData);
        log.info("Received creditDto: {}", creditDto);
        return creditDto;
    }

    private void saveCreditAndUpdateStatement(Statement statement, CreditDto creditDto) {
        Credit credit = new Credit();
        credit.setAmount(creditDto.amount());
        credit.setTerm(creditDto.term());
        credit.setMonthlyPayment(creditDto.monthlyPayment());
        credit.setRate(creditDto.rate());
        credit.setPsk(creditDto.psk());
        credit.setPaymentSchedule(creditDto.paymentSchedule());
        credit.setInsuranceEnabled(creditDto.insuranceEnabled());
        credit.setSalaryClient(creditDto.salaryClient());
        credit.setCreditStatus(CreditStatus.CALCULATED);
        creditRepository.save(credit);

        statement.setCredit(credit);
        statement.setStatus(ApplicationStatus.PREPARE_DOCUMENTS);
        statementRepository.save(statement);

        log.debug("Created credit and linked to statement: {}", credit);
    }
}
