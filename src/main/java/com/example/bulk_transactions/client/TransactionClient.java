package com.example.bulk_transactions.client;

import com.example.bulk_transactions.dto.TransactionRequest;
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
public class TransactionClient {
    private final WebClient webClient;

    public TransactionClient(WebClient.Builder webClientBuilder, @Value("${transaction-service.base-url}") String baseUrl) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
    }

    @Retry(name = "transactionServiceRetry")
    @CircuitBreaker(name = "transactionServiceCB", fallbackMethod = "fallbackTransaction")
    public void processTransaction(TransactionRequest transaction) {
        webClient.post()
                .uri("/api/v1/transactions")
                .bodyValue(transaction)
                .retrieve()
                .onStatus(HttpStatusCode::isError, resp -> resp.bodyToMono(String.class).flatMap(body -> Mono.error(new Exception(body))))
                .toBodilessEntity()
                .block();
    }

    public void fallbackTransaction(TransactionRequest transaction, Throwable t) throws Exception {
        log.warn("Fallback triggered for transaction {}: {}", transaction.getTransactionId(), t.toString());
        throw new Exception("TransactionService unavailable:" + t.getMessage());
    }
}
