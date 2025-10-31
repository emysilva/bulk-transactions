package com.example.bulk_transactions.service;

import com.example.bulk_transactions.client.TransactionServiceClient;
import com.example.bulk_transactions.dto.BulkTransactionRequest;
import com.example.bulk_transactions.dto.BulkTransactionResponse;
import com.example.bulk_transactions.dto.client.TransactionServiceRequest;
import com.example.bulk_transactions.dto.client.TransactionServiceResult;
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
    private final TransactionServiceClient transactionServiceClient;
    private final MeterRegistry meterRegistry;
    private final ExecutorService executor = Executors.newFixedThreadPool(10);

    public BulkTransactionService(TransactionServiceClient transactionServiceClient, MeterRegistry meterRegistry) {
        this.transactionServiceClient = transactionServiceClient;
        this.meterRegistry = meterRegistry;
    }

    public BulkTransactionResponse processBulkTransactions(@Valid BulkTransactionRequest request) {
        List<CompletableFuture<TransactionServiceResult>> futures = request.getTransactions()
                .stream()
                .map(transaction -> CompletableFuture.supplyAsync(() -> processSingleTransaction(request.getBatchId(), transaction), executor))
                .toList();

        List<TransactionServiceResult> results = futures.stream()
                .map(CompletableFuture::join)
                .toList();

        return new BulkTransactionResponse(request.getBatchId(), results);
    }

    private TransactionServiceResult processSingleTransaction(@NotBlank String batchId, TransactionServiceRequest transaction) {
        MDC.put("batchId", batchId);
        MDC.put("transactionId", transaction.getTransactionId());

        try {
            transactionServiceClient.processTransaction(transaction);
            log.info("Transaction succeeded");
            meterRegistry.counter("transactions.success.count").increment();
            return new TransactionServiceResult(transaction.getTransactionId(), "SUCCESS", null);
        } catch (Exception e) {
            log.error("Transaction failed: {}", e.getMessage());
            meterRegistry.counter("transactions.failure.count").increment();
            return new TransactionServiceResult(transaction.getTransactionId(), "FAILED", e.getMessage());
        } finally {
            MDC.clear();
        }
    }
}
