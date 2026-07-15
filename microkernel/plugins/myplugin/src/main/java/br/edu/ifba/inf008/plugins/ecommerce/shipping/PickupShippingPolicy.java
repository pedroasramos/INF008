package br.edu.ifba.inf008.plugins.ecommerce.shipping;

import br.edu.ifba.inf008.plugins.ecommerce.model.Order;

public class PickupShippingPolicy implements ShippingPolicy{
    private final double fixedPrice = 0;

    @Override
    public double calculateShipping(Order order) {
        return fixedPrice;
    }
}
