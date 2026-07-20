package br.edu.ifba.inf008.plugins.ecommerce.repository;

import br.edu.ifba.inf008.plugins.ecommerce.discount.CouponDiscountPolicy;
import br.edu.ifba.inf008.plugins.ecommerce.discount.DiscountPolicy;
import br.edu.ifba.inf008.plugins.ecommerce.discount.NoDiscountPolicy;
import br.edu.ifba.inf008.plugins.ecommerce.discount.StudentDiscountPolicy;
import br.edu.ifba.inf008.plugins.ecommerce.payment.BoletoPayment;
import br.edu.ifba.inf008.plugins.ecommerce.payment.CreditCardPayment;
import br.edu.ifba.inf008.plugins.ecommerce.payment.Payable;
import br.edu.ifba.inf008.plugins.ecommerce.payment.PixPayment;
import br.edu.ifba.inf008.plugins.ecommerce.shipping.EconomyShippingPolicy;
import br.edu.ifba.inf008.plugins.ecommerce.shipping.ExpressShippingPolicy;
import br.edu.ifba.inf008.plugins.ecommerce.shipping.PickupShippingPolicy;
import br.edu.ifba.inf008.plugins.ecommerce.shipping.ShippingPolicy;
import br.edu.ifba.inf008.plugins.ecommerce.shipping.StandardShippingPolicy;

public class PolicyTypeResolver {
    public static String resolveDiscountType(DiscountPolicy policy) {
        if (policy instanceof CouponDiscountPolicy) return "COUPON";
        if (policy instanceof StudentDiscountPolicy) return "STUDENT";
        if (policy instanceof NoDiscountPolicy) return "NONE";
        throw new IllegalArgumentException("Unmapped discount type: " + policy.getClass());
    }

    public static String resolveShippingType(ShippingPolicy policy) {
        if (policy instanceof ExpressShippingPolicy) return "EXPRESS";
        if (policy instanceof PickupShippingPolicy) return "PICKUP";
        if (policy instanceof StandardShippingPolicy) return "STANDARD";
        if (policy instanceof EconomyShippingPolicy) return "ECONOMY";
        throw new IllegalArgumentException("Unmapped shipping type: " + policy.getClass());
    }

    public static String resolvePaymentType(Payable payment) {
        if (payment instanceof BoletoPayment) return "BOLETO";
        if (payment instanceof CreditCardPayment) return "CREDIT_CARD";
        if (payment instanceof PixPayment) return "PIX";
        throw new IllegalArgumentException("Unmapped payment type: " + payment.getClass());
    }
}
