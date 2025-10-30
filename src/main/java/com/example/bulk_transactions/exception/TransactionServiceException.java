package com.example.bulk_transactions.exception;

public class TransactionServiceException extends RuntimeException {

    private final int statusCode;
    private final String serviceName;

    public TransactionServiceException(String message) {
        super(message);
        this.statusCode = 500;
        this.serviceName = "TransactionService";
    }

    public TransactionServiceException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
        this.serviceName = "TransactionService";
    }

    public TransactionServiceException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = 500;
        this.serviceName = "TransactionService";
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getServiceName() {
        return serviceName;
    }
}