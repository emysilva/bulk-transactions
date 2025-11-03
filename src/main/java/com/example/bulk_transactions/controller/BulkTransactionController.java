package com.example.bulk_transactions.controller;

import com.example.bulk_transactions.dto.BulkTransactionRequest;
import com.example.bulk_transactions.dto.BulkTransactionResponse;
import com.example.bulk_transactions.service.BulkTransactionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/bulk-transactions")
public class BulkTransactionController {

    private final BulkTransactionService service;

    public BulkTransactionController(BulkTransactionService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<BulkTransactionResponse> processBulkTransactions(@Valid @RequestBody BulkTransactionRequest request) {
        BulkTransactionResponse response = service.processBulkTransactions(request);
        return ResponseEntity.ok(response);
    }
}
