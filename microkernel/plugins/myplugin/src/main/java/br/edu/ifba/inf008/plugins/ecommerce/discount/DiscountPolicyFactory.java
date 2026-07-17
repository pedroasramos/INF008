package br.edu.ifba.inf008.plugins.ecommerce.discount;

public class DiscountPolicyFactory {
    public static DiscountPolicy fromRow(String type, String coupon) {
        switch (type){
            case "COUPON": return new CouponDiscountPolicy(coupon);
            case "STUDENT": return new StudentDiscountPolicy();
            default: throw new IllegalArgumentException("Unknown discount type: " + type);
        }
    }
}
