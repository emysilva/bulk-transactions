package com.example.bulk_transactions.exception;

import lombok.Getter;

@Getter
public class TransactionServiceException extends RuntimeException {

    private final int statusCode;
    private final String serviceName;

    public TransactionServiceException(String message) {
        super(message);
        this.statusCode = 500;
        this.serviceName = "TransactionService";
    }

}