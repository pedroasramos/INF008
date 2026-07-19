package br.edu.ifba.inf008.plugins.ecommerce.payment;

public class PayableFactory {
    public static Payable fromRow(String type, String barcode, String dueDate,
                                  Long cardNumber, String cardHolder, String cardExpiration,
                                  String pixKey) {
        switch (type) {
            case "BOLETO": return new BoletoPayment(barcode, dueDate);
            case "CREDIT_CARD": return new CreditCardPayment(cardNumber, cardHolder, 0, cardExpiration); // cvv is not persisted
            case "PIX": return new PixPayment(pixKey);
            default: throw new IllegalArgumentException("Unknown payment type: " + type);
        }
    }
}
