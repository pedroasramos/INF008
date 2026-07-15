package br.edu.ifba.inf008.plugins.ecommerce.payment;

import br.edu.ifba.inf008.plugins.ecommerce.model.OrderStatus;

import java.util.UUID;

public class PixPayment implements Payable{
    private String pixKey;
    private OrderStatus orderStatus;

    public PixPayment(String pixKey) {
        this.pixKey = pixKey;
    }

    @Override
    public PaymentStatus pay(double amount) {
        if (validate()){
            return PaymentStatus.PAID;
        }
        return PaymentStatus.INVALID;
    }

    @Override
    protected boolean validate() {
        try {
            return (isCPF(pixKey)
                || isCNPJ(pixKey)
                || isEmail(pixKey)
                || isPhone(pixKey)
                || isRandomKey(pixKey));

        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isCPF(String key){
        return key.matches("\\d{11}");
    }

    private boolean isCNPJ(String key){
        return key.matches("\\d{14}");
    }

    private boolean isEmail(String key){
        return key.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }

    private boolean isPhone(String key){
        return key.matches("^\\+[1-9]\\d{1,14}$");
    }

    private boolean isRandomKey(String key) {
        try {
            UUID.fromString(key);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
