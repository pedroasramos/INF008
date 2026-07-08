package br.edu.ifba.inf008;

import java.util.HashMap;
import java.util.Map;

public class Cart {

    private static int cart_id = 1;
    private int customer_id;
    Map<Integer, Map<Integer, Integer>> cartsAll = new HashMap<>();
    Map<Integer, Integer> items = new HashMap<>();

    Product product = new Product();

    public Cart(){}

    public Cart(int customer_id) {
        this.customer_id = customer_id;
        cart_id++;
    }

    public void cartAdd(int product_id, int amount){
        Product productFind = product.search(product_id);
        if(productFind.getStock() > amount){
            items.put(product_id, amount);
            cartsAll.put(cart_id, items);
        }
    }

    public void cartRemove(int product_id){
        items.remove(product_id);
        cartsAll.remove(cart_id);
    }

    public Map<Integer, Integer> getCart(int cart_id){
        return cartsAll.get(cart_id);
    }


}
