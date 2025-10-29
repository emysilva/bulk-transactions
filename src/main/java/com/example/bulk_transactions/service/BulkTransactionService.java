package com.example.bulk_transactions.service;

import com.example.bulk_transactions.client.TransactionClient;
import com.example.bulk_transactions.dto.BulkTransactionRequest;
import com.example.bulk_transactions.dto.BulkTransactionResponse;
import com.example.bulk_transactions.dto.TransactionRequest;
import com.example.bulk_transactions.dto.TransactionResult;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@Slf4j
public class BulkTransactionService {
    private final TransactionClient transactionClient;
    private final MeterRegistry meterRegistry;
    private final ExecutorService executor = Executors.newFixedThreadPool(10);

    public BulkTransactionService(TransactionClient transactionClient, MeterRegistry meterRegistry) {
        this.transactionClient = transactionClient;
        this.meterRegistry = meterRegistry;
    }

    @CircuitBreaker(name = "transactionService", fallbackMethod = "fallbackProcess")
    @Retry(name = "transactionService")
    public BulkTransactionResponse processBulkTransactions(@Valid BulkTransactionRequest request) {
        List<CompletableFuture<TransactionResult>> futures = request.getTransactions()
                .stream()
                .map(transaction -> CompletableFuture.supplyAsync(() -> processSingleTransaction(request.getBatchId(), transaction), executor))
                .toList();

        List<TransactionResult> results = futures.stream()
                .map(CompletableFuture::join)
                .toList();

        return new BulkTransactionResponse(request.getBatchId(), results);
    }

    private TransactionResult processSingleTransaction(@NotBlank String batchId, TransactionRequest transaction) {
        MDC.put("batchId", batchId);
        MDC.put("transactionId", transaction.getTransactionId());

        try {
            transactionClient.processTransaction(transaction);
            log.info("Transaction succeeded");
            meterRegistry.counter("transactions.success.count").increment();
            return new TransactionResult(transaction.getTransactionId(), "SUCCESS", null);
        } catch (Exception e) {
            log.error("Transaction failed: {}", e.getMessage());
            meterRegistry.counter("transactions.failure.count").increment();
            return new TransactionResult(transaction.getTransactionId(), "FAILED", e.getMessage());
        } finally {
            MDC.clear();
        }
    }
}
