package com.example.bulk_transactions.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class BulkTransactionResponse {
    private String batchId;
    private List<TransactionServiceResult> results;
}
