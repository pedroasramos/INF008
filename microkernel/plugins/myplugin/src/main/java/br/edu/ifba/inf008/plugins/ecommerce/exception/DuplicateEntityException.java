package br.edu.ifba.inf008.plugins.ecommerce.exception;

public class DuplicateEntityException extends DomainException {
    public DuplicateEntityException(String message) {
        super(message);
    }
}
