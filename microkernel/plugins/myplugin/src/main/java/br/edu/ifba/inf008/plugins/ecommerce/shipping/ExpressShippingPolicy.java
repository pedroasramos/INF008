package br.edu.ifba.inf008.plugins.ecommerce.shipping;

import br.edu.ifba.inf008.plugins.ecommerce.model.Order;

public class ExpressShippingPolicy implements ShippingPolicy{
    private final double fixedPrice = 45;

    @Override
    public double calculateShipping(Order order) {
        return fixedPrice;
    }
}
