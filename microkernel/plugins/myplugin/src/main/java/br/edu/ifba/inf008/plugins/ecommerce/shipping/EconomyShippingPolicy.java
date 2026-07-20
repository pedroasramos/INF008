package br.edu.ifba.inf008.plugins.ecommerce.shipping;

import br.edu.ifba.inf008.plugins.ecommerce.model.Order;

public class EconomyShippingPolicy implements ShippingPolicy{
    private final double fixedPrice = 15;

    @Override
    public double calculateShipping(Order order) {
        return fixedPrice;
    }
}