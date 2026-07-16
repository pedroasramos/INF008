package br.edu.ifba.inf008.plugins.ecommerce.payment;

import br.edu.ifba.inf008.plugins.ecommerce.model.OrderStatus;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

public class BankSlipPayment implements Payable{
    private String barcode;
    private String dueDate;
    private final String formatter = "dd/MM/yyyy";

    public BankSlipPayment(String barcode, String dueDate) {
        this.barcode = barcode;
        this.dueDate = dueDate;
    }

    @Override
    public PaymentStatus pay(double amount) {
        if (validate()){
            if(amount <= 0){
                return PaymentStatus.FAILED;
            }
            return PaymentStatus.PAID;
        }
        return PaymentStatus.PENDING;
    }

    @Override
    public boolean validate() {
        try {
            DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern(formatter)
                    .withResolverStyle(ResolverStyle.STRICT);
            LocalDate expiration = LocalDate.parse(dueDate, formatter1);
            LocalDate today = LocalDate.now();
            if (expiration.isAfter(today)) {
                if (barcode.length() == 47 || barcode.length() == 48) {
                    return true;
                }
            }
        } catch (DateTimeParseException e) {
            return false;
        }
        return false;
    }
}
