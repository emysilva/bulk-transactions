package com.example.bulk_transactions.dto;

import com.example.bulk_transactions.dto.client.TransactionServiceRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class BulkTransactionRequest {
    @NotBlank
    private String batchId;

    @NotEmpty
    @Valid
    private List<TransactionServiceRequest> transactions;
}
