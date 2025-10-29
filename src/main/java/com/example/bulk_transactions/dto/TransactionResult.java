package com.example.bulk_transactions.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TransactionResult {
    private String transactionId;
    private String status;
    private String reason;
}
