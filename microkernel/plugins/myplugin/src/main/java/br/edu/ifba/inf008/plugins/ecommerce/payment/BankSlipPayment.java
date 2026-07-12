package br.edu.ifba.inf008.plugins.ecommerce.payment;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

public class BankSlipPayment implements Payable{
    private long barcode;
    private String dueDate;
    private final String formatter = "dd/MM/yyyy";

    public BankSlipPayment(long barcode, String dueDate) {
        this.barcode = barcode;
        this.dueDate = dueDate;
    }

    @Override
    public boolean pay() {
        if (validate()){
            return true;
        }
        return false;
    }

    @Override
    public boolean validate() {
        try{
            DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern(formatter)
                    .withResolverStyle(ResolverStyle.STRICT);
            LocalDate.parse(dueDate, formatter1);
            return true;
        } catch (DateTimeParseException e){
            return false;
        }
    }
}
