package br.edu.ifba.inf008.plugins.ecommerce.payment;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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
    public PaymentStatus pay(double amount) {
        if (validate()){
            if(amount <= 0){
                return PaymentStatus.FAILED;
            }
            return PaymentStatus.PAID;
        }
        return PaymentStatus.INVALID;
    }

    @Override
    public boolean validate() {
        try {
            // SMART (not STRICT): with the "yy" pattern letter, java.time resolves the parsed
            // field as YearOfEra, which YearMonth.from(...) cannot consume under STRICT (it only
            // accepts the proleptic YEAR field), so every date would fail to parse. SMART performs
            // that YearOfEra -> YEAR conversion while still rejecting out-of-range months/years.
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(template)
                    .withResolverStyle(ResolverStyle.SMART);
            YearMonth expiration = YearMonth.parse(expirationDate, formatter);
            return !expiration.isBefore(YearMonth.now());
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    public long getCardNumber() {
        return cardNumber;
    }

    public String getHolder() {
        return holder;
    }

    public String getExpirationDate() {
        return expirationDate;
    }
}
