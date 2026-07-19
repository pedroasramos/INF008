package br.edu.ifba.inf008.plugins.ecommerce.model;

import java.util.ArrayList;
import java.util.List;

public class Cart {

    private int cart_id;
    private int customerId;
    private String status;
    private List<CartItem> items = new ArrayList<>();

    public Cart() {
        this.status = "OPEN";
    }

    public Cart(int customerId) {
        this();
        this.customerId = customerId;
    }

    public int getCart_id() {
        return cart_id;
    }

    public void setCart_id(int cart_id) {
        this.cart_id = cart_id;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void addProduct(Product product, int quantity){
        CartItem item = new CartItem(product, quantity);
        items.add(item);
    }

    public void removeProduct(Product product){
        items.removeIf(item -> item.getProduct().getProduct_id() == product.getProduct_id());
    }

    public void clear(){
        items.clear();
    }

    public double calculateSubtotal(){
        double sum = 0;
        for(CartItem cartItem : items){
            sum += cartItem.calculateSubtotal();
        }
        return sum;
    }

    public List<CartItem> getItems() {
        return items;
    }
}
