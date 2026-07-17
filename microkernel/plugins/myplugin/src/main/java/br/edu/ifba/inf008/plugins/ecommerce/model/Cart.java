package br.edu.ifba.inf008.plugins.ecommerce.model;

import java.util.ArrayList;
import java.util.List;

public class Cart {

    private int cart_id;
    private List<CartItem> items = new ArrayList<>();

    public int getCart_id() {
        return cart_id;
    }

    public void setCart_id(int cart_id) {
        this.cart_id = cart_id;
    }

    public void addProduct(Product product, int quantity){
        CartItem item = new CartItem(product, quantity);
        items.add(item);
    }

    public void removeProduct(Product product){
        items.remove(product);
    }

    public void clear(){
        items.clear();
    }

    public double calculateSubtotal(){
        double sum = 0;
        for(CartItem cartItem : items){
            sum += cartItem.getQuantity();
        }
        return sum;
    }

    public List<CartItem> getItems() {
        return items;
    }
}
