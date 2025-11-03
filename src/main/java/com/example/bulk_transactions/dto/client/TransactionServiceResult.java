package com.example.bulk_transactions.dto.client;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TransactionServiceResult {
    @NotBlank(message = "TransactionId is required")
    private String transactionId;
    @NotBlank(message = "Status is required")
    private String status;
    private String reason;
}
