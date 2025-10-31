package com.example.bulk_transactions.client;

import com.example.bulk_transactions.dto.client.TransactionServiceRequest;
import com.example.bulk_transactions.exception.TransactionServiceException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class TransactionServiceClient {
    private final WebClient webClient;

    public TransactionServiceClient(WebClient.Builder webClientBuilder, @Value("${transaction-service.base-url}") String baseUrl) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
    }

    @Retry(name = "transaction-service-retry")
    @CircuitBreaker(name = "transaction-service-cb", fallbackMethod = "fallbackTransaction")
    public void processTransaction(TransactionServiceRequest transaction) {
        webClient.post()
                .uri("/api/v1/transactions")
                .bodyValue(transaction)
                .retrieve()
                .onStatus(HttpStatusCode::isError, resp -> resp.bodyToMono(String.class).flatMap(body -> Mono.error(new TransactionServiceException(body))))
                .toBodilessEntity()
                .block();
    }

    public void fallbackTransaction(TransactionServiceRequest transaction, Throwable t) throws Exception {
        log.warn("Fallback triggered for transaction {}: {}", transaction.getTransactionId(), t.toString());
        throw new TransactionServiceException("TransactionService unavailable:" + t.getMessage());
    }
}
