package com.example.bulk_transactions.service;

import com.example.bulk_transactions.client.TransactionServiceClient;
import com.example.bulk_transactions.dto.BulkTransactionRequest;
import com.example.bulk_transactions.dto.BulkTransactionResponse;
import com.example.bulk_transactions.dto.TransactionServiceRequest;
import com.example.bulk_transactions.exception.TransactionServiceException;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class BulkTransactionServiceTest {

    private TransactionServiceClient transactionServiceClient;
    private SimpleMeterRegistry meterRegistry;
    private BulkTransactionService bulkTransactionService;

    @BeforeEach
    void setUp() {
        transactionServiceClient = Mockito.mock(TransactionServiceClient.class);
        meterRegistry = new SimpleMeterRegistry();
        bulkTransactionService = new BulkTransactionService(transactionServiceClient, meterRegistry);
    }

    private BulkTransactionRequest buildRequest() {
        TransactionServiceRequest tx1 = new TransactionServiceRequest("tx-001", "acct1", "acct2", BigDecimal.valueOf(100));
        TransactionServiceRequest tx2 = new TransactionServiceRequest("tx-002", "acct1", "acct3", BigDecimal.valueOf(200));
        return new BulkTransactionRequest("batch-001", List.of(tx1, tx2));
    }

    @Test
    void testAllTransactionsSuccess() {
        BulkTransactionRequest request = buildRequest();

        doNothing().when(transactionServiceClient).processTransaction(any(TransactionServiceRequest.class));

        BulkTransactionResponse response = bulkTransactionService.processBulkTransactions(request);

        assertThat(response).isNotNull();
        assertThat(response.getResults()).hasSize(2);
        assertThat(response.getResults().stream().allMatch(r -> r.getStatus().equals("SUCCESS"))).isTrue();

        verify(transactionServiceClient, times(2)).processTransaction(any(TransactionServiceRequest.class));
        assertThat(meterRegistry.counter("transactions.success.count").count()).isEqualTo(2);
        assertThat(meterRegistry.counter("transactions.failure.count").count()).isEqualTo(0);
    }

    @Test
    void testPartialFailure() {
        BulkTransactionRequest request = buildRequest();

        doNothing().when(transactionServiceClient).processTransaction(request.getTransactions().get(0));
        doThrow(new TransactionServiceException("Transaction service unavailable"))
                .when(transactionServiceClient)
                .processTransaction(request.getTransactions().get(1));

        BulkTransactionResponse response = bulkTransactionService.processBulkTransactions(request);

        assertThat(response.getResults()).hasSize(2);
        assertThat(response.getResults().stream().anyMatch(r -> r.getStatus().equals("FAILED"))).isTrue();

        assertThat(meterRegistry.counter("transactions.success.count").count()).isEqualTo(1);
        assertThat(meterRegistry.counter("transactions.failure.count").count()).isEqualTo(1);
    }

    @Test
    void testAllTransactionsFail() {
        BulkTransactionRequest request = buildRequest();

        doThrow(new TransactionServiceException("Transaction service unavailable"))
                .when(transactionServiceClient)
                .processTransaction(any(TransactionServiceRequest.class));

        BulkTransactionResponse response = bulkTransactionService.processBulkTransactions(request);

        assertThat(response.getResults().stream().allMatch(r -> r.getStatus().equals("FAILED"))).isTrue();
        verify(transactionServiceClient, times(2)).processTransaction(any(TransactionServiceRequest.class));

        assertThat(meterRegistry.counter("transactions.success.count").count()).isEqualTo(0);
        assertThat(meterRegistry.counter("transactions.failure.count").count()).isEqualTo(2);
    }
}
