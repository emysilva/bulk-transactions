package com.example.bulk_transactions.dto;

import com.example.bulk_transactions.dto.client.TransactionServiceResult;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class BulkTransactionResponse {
    @NotBlank(message = "BatchId is required")
    private String batchId;
    @NotEmpty(message = "At least one result is required")
    @Valid
    private List<TransactionServiceResult> results;
}
