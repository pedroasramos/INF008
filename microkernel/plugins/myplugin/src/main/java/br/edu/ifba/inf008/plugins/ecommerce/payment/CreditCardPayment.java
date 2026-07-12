package br.edu.ifba.inf008.plugins.ecommerce.payment;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;

public class CreditCardPayment implements Payable{
    private long cardNumber;
    private String holder;
    private int cvv;
    private String expirationDate;
    private final String template = "MM/yy";

    public CreditCardPayment(long cardNumber, String holder, int cvv, String expirationDate) {
        this.cardNumber = cardNumber;
        this.holder = holder;
        this.cvv = cvv;
        this.expirationDate = expirationDate;
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
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(template)
                    .withResolverStyle(ResolverStyle.STRICT);
            YearMonth date = YearMonth.parse(expirationDate, formatter);
            return true;
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }
}
