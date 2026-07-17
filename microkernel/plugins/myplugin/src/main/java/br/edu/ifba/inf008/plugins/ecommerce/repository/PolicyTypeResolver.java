package br.edu.ifba.inf008.plugins.ecommerce.repository;

import br.edu.ifba.inf008.plugins.ecommerce.discount.CouponDiscountPolicy;
import br.edu.ifba.inf008.plugins.ecommerce.discount.DiscountPolicy;
import br.edu.ifba.inf008.plugins.ecommerce.discount.StudentDiscountPolicy;
import br.edu.ifba.inf008.plugins.ecommerce.payment.BankSlipPayment;
import br.edu.ifba.inf008.plugins.ecommerce.payment.CreditCardPayment;
import br.edu.ifba.inf008.plugins.ecommerce.payment.Payable;
import br.edu.ifba.inf008.plugins.ecommerce.payment.PixPayment;
import br.edu.ifba.inf008.plugins.ecommerce.shipping.ExpressShippingPolicy;
import br.edu.ifba.inf008.plugins.ecommerce.shipping.PickupShippingPolicy;
import br.edu.ifba.inf008.plugins.ecommerce.shipping.ShippingPolicy;
import br.edu.ifba.inf008.plugins.ecommerce.shipping.StandardShinppingPolicy;

public class PolicyTypeResolver {
    public static String resolveDiscountType(DiscountPolicy policy) {
        if (policy instanceof CouponDiscountPolicy) return "COUPON";
        if (policy instanceof StudentDiscountPolicy) return "STUDENT";
        throw new IllegalArgumentException("Tipo de desconto não mapeado: " + policy.getClass());
    }

    public static String resolveShippingType(ShippingPolicy policy) {
        if (policy instanceof ExpressShippingPolicy) return "EXPRESS";
        if (policy instanceof PickupShippingPolicy) return "PICKUP";
        if (policy instanceof StandardShinppingPolicy) return "STANDARD";
        throw new IllegalArgumentException("Tipo de frete não mapeado: " + policy.getClass());
    }

    public static String resolvePaymentType(Payable payment) {
        if (payment instanceof BankSlipPayment) return "BANK_SLIP";
        if (payment instanceof CreditCardPayment) return "CREDIT_CARD";
        if (payment instanceof PixPayment) return "PIX";
        throw new IllegalArgumentException("Tipo de pagamento não mapeado: " + payment.getClass());
    }
}
