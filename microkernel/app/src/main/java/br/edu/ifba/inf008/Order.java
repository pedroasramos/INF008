package br.edu.ifba.inf008;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Order {

    private static int order_id = 1;
    private int customer_id;
    private int cart_id;
    Map<Integer, Integer> orderAll = new HashMap<>();
    Map<Integer, List<Product>> products = new HashMap<>();
    Map<Integer, List<Double>> prices = new HashMap<>();
    Map<Integer, List<Integer>> quantities = new HashMap<>();
    Cart cart = new Cart();
    Product product = new Product();

    public Order(int customer_id, int cart_id) {
        this.customer_id = customer_id;
        this.cart_id = cart_id;
        orderAll.put(customer_id, cart_id);
        Map<Integer, Integer> cartTemp = cart.getCart(cart_id);
        for(Map.Entry<Integer, Integer> entry : cartTemp.entrySet()) {
            Integer productID = entry.getKey();
            Integer quantity = entry.getValue();
            Product product1 = product.search(productID);
            products.computeIfAbsent(order_id, k -> new ArrayList<>()).add(product1);
            prices.computeIfAbsent(order_id, k -> new ArrayList<>()).add(product1.getPrice());
            quantities.computeIfAbsent(order_id, k -> new ArrayList<>()).add(quantity);
        }
    }
}