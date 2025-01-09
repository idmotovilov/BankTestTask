package org.example.service;

import lombok.extern.slf4j.Slf4j;
import org.example.exception.DealServiceException;
import org.springframework.transaction.annotation.Transactional;

/**
 * Универсальный шаблон для выполнения операций в сервисах.
 * Здесь можно централизовать логику валидации, транзакционности и обработки исключений.
 *
 * @param <REQ> тип входного запроса
 * @param <RES> тип результата
 */
@Slf4j
public abstract class DealServiceTemplate<REQ, RES> {

    /**
     * Метод, отвечающий за общий жизненный цикл операции:
     * - логгирование старта,
     * - транзакционное выполнение основной логики {@link #process(Object)},
     * - логгирование успешного завершения,
     * - отлавливание и проброс DealServiceException при ошибках.
     *
     * @param request входной запрос
     * @return результат выполнения операции
     */
    @Transactional
    public RES execute(REQ request) {
        log.info("Starting service operation with request: {}", request);
        try {
            validate(request);
            RES result = process(request);
            log.info("Service operation completed successfully for request: {}", request);
            return result;
        } catch (Exception e) {
            log.error("Error during service operation for request: {}", request, e);
            throw new DealServiceException("Failed to execute service operation", e);
        }
    }

    /**
     * Валидация входных данных (необязательно; по умолчанию пустая реализация).
     *
     * @param request входной запрос
     */
    protected void validate(REQ request) {
        // Можно переопределять в наследниках при необходимости
    }

    /**
     * Основная бизнес-логика обработки входного запроса.
     *
     * @param request входной запрос
     * @return результат выполнения операции
     */
    protected abstract RES process(REQ request);
}
