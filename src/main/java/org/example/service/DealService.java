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

import java.time.LocalDate;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Slf4j
@Service
@RequiredArgsConstructor
public class DealService {

    private final ClientRepository clientRepository;
    private final StatementRepository statementRepository;
    private final CreditRepository creditRepository;
    private final PassportRepository passportRepository;
    private final EmploymentRepository employmentRepository;
    private final CalculatorClient calculatorClient;

    /**
     * Создание Statement, получение офферов
     */
    public List<LoanOfferDto> createStatementAndGetOffers(LoanStatementRequestDto dto) {
        log.info("createStatementAndGetOffers called with dto: {}", dto);
        try {

            Client client = ClientMapper.INSTANCE.toEntity(dto);

            // Если переданы паспортные данные
            if (dto.passportSeries() != null && dto.passportNumber() != null) {
                Passport passport = new Passport();
                passport.setSeries(dto.passportSeries());
                passport.setNumber(dto.passportNumber());
                passportRepository.save(passport);

                client.setPassport(passport);
            }

            clientRepository.save(client);
            log.debug("Saved Client: {}", client);

            // Создаём Statement
            Statement statement = new Statement();
            statement.setClient(client);
            statement.setStatus(ApplicationStatus.PREAPPROVAL);
            statement.setCreationDate(LocalDate.now());
            statementRepository.save(statement);

            // Формируем ScoringDataDto
            ScoringDataDto scoringData = new ScoringDataDto(
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

            // Запрашиваем офферы из калькулятора
            List<LoanOfferDto> offers = calculatorClient.getLoanOffers(scoringData);

            // Проставляем statementId
            List<LoanOfferDto> updatedOffers = offers.stream()
                    .map(offer -> new LoanOfferDto(
                            statement.getStatementId(),
                            offer.requestedAmount(),
                            offer.term(),
                            offer.monthlyPayment(),
                            offer.rate(),
                            offer.insuranceEnabled(),
                            offer.salaryClient()
                    ))
                    .collect(toList());

            log.info("Returning offers: {}", updatedOffers);
            return updatedOffers;
        } catch (Exception e) {
            log.error("Error in createStatementAndGetOffers", e);
            throw new DealServiceException("Failed to create statement or get offers", e);
        }
    }

    /**
     * Выбор оффера
     */
    public void selectOffer(LoanOfferDto offer) {
        log.info("selectOffer called with offer: {}", offer);
        try {
            Statement statement = statementRepository.findById(offer.statementId())
                    .orElseThrow(() -> new DealServiceException("Statement not found. Id=" + offer.statementId()));

            statement.setStatus(ApplicationStatus.APPROVED);

            // Сохраняем LoanOfferDto в строку
            String appliedOfferJson = offer.toString();
            statement.setAppliedOffer(appliedOfferJson);
            statementRepository.save(statement);
        } catch (Exception e) {
            log.error("Error in selectOffer", e);
            throw new DealServiceException("Failed to select offer for statementId=" + offer.statementId(), e);
        }
    }

    /**
     * Завершение регистрации + полный подсчёт кредита
     */
    public void finishRegistration(Long statementId, FinishRegistrationRequestDto dto) {
        log.info("finishRegistration called with statementId={}, dto={}", statementId, dto);
        try {
            Statement statement = statementRepository.findById(statementId)
                    .orElseThrow(() -> new DealServiceException("Statement not found. Id=" + statementId));

            Client client = statement.getClient();

            // Устанавливаем поля
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

            // Employment
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

            // Логируем appliedOffer
            String appliedOfferString = statement.getAppliedOffer();
            log.info("Statement has appliedOffer: {}", appliedOfferString);

            // Вызываем калькулятор
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

            CreditDto creditDto = calculatorClient.calculateCredit(scoringData);
            log.info("Received CreditDto from calculator: {}", creditDto);

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

            log.info("finishRegistration completed successfully");
        } catch (Exception e) {
            log.error("Error in finishRegistration", e);
            throw new DealServiceException("Failed to finish registration for statementId=" + statementId, e);
        }
    }
}
