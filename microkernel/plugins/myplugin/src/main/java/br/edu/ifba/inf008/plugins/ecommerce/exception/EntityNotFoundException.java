package br.edu.ifba.inf008.plugins.ecommerce.exception;

public class EntityNotFoundException extends DomainException{
    public EntityNotFoundException(String entityName, int id) {
        super(entityName + " with ID " + id + " not found.");
    }
    public EntityNotFoundException(String message){
        super(message);
    }
}
