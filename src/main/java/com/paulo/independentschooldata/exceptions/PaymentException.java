package com.paulo.independentschooldata.exceptions;

public class PaymentException extends RuntimeException {

    private final String stripeErrorCode;

    public PaymentException(String message) {
        super(message);
        this.stripeErrorCode = null;
    }

    public PaymentException(String message, String stripeErrorCode) {
        super(message);
        this.stripeErrorCode = stripeErrorCode;
    }

    public PaymentException(String message, Throwable cause) {
        super(message, cause);
        this.stripeErrorCode = null;
    }

    public String getStripeErrorCode() {
        return stripeErrorCode;
    }
}
