package br.edu.ifba.inf008.plugins.ecommerce.exception;

public class InsufficientStockException extends DomainException {
    public InsufficientStockException(String productName, int requested, int available) {
        super("Insufficient stock for product '" + productName + "': requested " + requested
                + ", available " + available + ".");
    }
}