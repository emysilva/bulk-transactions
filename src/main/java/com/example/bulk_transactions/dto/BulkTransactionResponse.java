package com.example.bulk_transactions.dto;

import com.example.bulk_transactions.dto.client.TransactionServiceResult;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class BulkTransactionResponse {
    private String batchId;
    private List<TransactionServiceResult> results;
}
