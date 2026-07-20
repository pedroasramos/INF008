package br.edu.ifba.inf008.plugins.ecommerce.payment;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

public class BoletoPayment implements Payable{
    private String barcode;
    private String dueDate;
    private final String formatter = "dd/MM/yyyy";

    public BoletoPayment(String barcode, String dueDate) {
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
            // SMART (not STRICT): the "yyyy" pattern letter resolves to YearOfEra, which
            // LocalDate.from(...) cannot consume under STRICT (it only accepts the proleptic
            // YEAR field), so every date would fail to parse. SMART performs that conversion
            // while still rejecting out-of-range days/months.
            DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern(formatter)
                    .withResolverStyle(ResolverStyle.SMART);
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

    public String getBarcode() {
        return barcode;
    }

    public String getDueDate() {
        return dueDate;
    }
}
