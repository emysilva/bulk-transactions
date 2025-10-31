package com.example.bulk_transactions.dto.client;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TransactionServiceResult {
    private String transactionId;
    private String status;
    private String reason;
}
