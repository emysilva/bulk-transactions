package com.example.bulk_transactions.service;

import com.example.bulk_transactions.client.TransactionServiceClient;
import com.example.bulk_transactions.dto.BulkTransactionRequest;
import com.example.bulk_transactions.dto.BulkTransactionResponse;
import com.example.bulk_transactions.dto.client.TransactionServiceRequest;
import com.example.bulk_transactions.dto.client.TransactionServiceResult;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
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
    private final ExecutorService executor = Executors.newFixedThreadPool(3);

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

    private TransactionServiceResult processSingleTransaction(String batchId, TransactionServiceRequest transaction) {
        try {
            transactionServiceClient.processTransaction(transaction);
            log.info("Transaction succeeded for batchId {} transactionId {}", batchId, transaction.getTransactionId());
            meterRegistry.counter("transactions.success.count").increment();
            return new TransactionServiceResult(transaction.getTransactionId(), "SUCCESS", null);
        } catch (Exception e) {
            log.error("Transaction failed for batchId {} transactionId {}: {}", batchId, transaction.getTransactionId(), e.getMessage());
            meterRegistry.counter("transactions.failure.count").increment();
            return new TransactionServiceResult(transaction.getTransactionId(), "FAILED", e.getMessage());
        }
    }
}
