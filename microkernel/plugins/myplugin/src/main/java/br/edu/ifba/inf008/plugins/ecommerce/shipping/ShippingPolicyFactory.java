package br.edu.ifba.inf008.plugins.ecommerce.shipping;

public class ShippingPolicyFactory {
    public static ShippingPolicy fromType(String type) {
        switch (type) {
            case "EXPRESS": return new ExpressShippingPolicy();
            case "PICKUP": return new PickupShippingPolicy();
            case "STANDARD": return new StandardShippingPolicy();
            case "ECONOMY": return new EconomyShippingPolicy();
            default: throw new IllegalArgumentException("Unknown shipping method: " + type);
        }
    }
}
