package com.example.bulk_transactions.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class TransactionServiceRequest {
    @NotBlank
    private String transactionId;

    @NotBlank
    private String fromAccount;

    @NotBlank
    private String toAccount;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal amount;
}
