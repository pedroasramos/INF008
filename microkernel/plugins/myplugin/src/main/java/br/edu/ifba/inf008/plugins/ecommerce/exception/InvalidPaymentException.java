package br.edu.ifba.inf008.plugins.ecommerce.exception;

public class InvalidPaymentException extends DomainException {
    public InvalidPaymentException(String message) {
        super(message);
    }
}