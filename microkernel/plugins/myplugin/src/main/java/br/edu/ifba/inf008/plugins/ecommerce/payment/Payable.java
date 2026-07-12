package br.edu.ifba.inf008.plugins.ecommerce.payment;

public interface Payable {
    public boolean pay();
    public boolean validate();

}
