package com.example.bulk_transactions.dto.client;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class TransactionServiceRequest {
    @NotBlank(message = "TransactionId is required")
    private String transactionId;

    @NotBlank(message = "Source account is required")
    private String fromAccount;

    @NotBlank(message = "Destination account is required")
    private String toAccount;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal amount;
}
