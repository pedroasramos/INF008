package br.edu.ifba.inf008.plugins.ecommerce.payment;


public interface Payable {
    public PaymentStatus pay(double amount);
    public boolean validate();
}
