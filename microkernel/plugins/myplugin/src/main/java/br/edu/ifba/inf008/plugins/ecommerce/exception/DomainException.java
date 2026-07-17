package br.edu.ifba.inf008.plugins.ecommerce.exception;

public class DomainException extends RuntimeException {
    public DomainException(String message) {
        super(message);
    }
    public DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
