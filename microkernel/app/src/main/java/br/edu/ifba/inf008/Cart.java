package br.edu.ifba.inf008;

import java.util.HashMap;
import java.util.Map;

public class Cart {

    private static int carrinho_id = 1;
    private int customer_id;
    Map<Integer, Map<Integer, Integer>> cartsAll = new HashMap<>();
    Map<Integer, Integer> items = new HashMap<>();

    Product product = new Product();


    public Cart(int customer_id) {
        this.customer_id = customer_id;
        carrinho_id++;
    }

    public void cartAdd(int product_id, int amount){
        Product productFind = product.search(product_id);
        if(productFind.getStock() > amount){
            items.put(product_id, amount);
            cartsAll.put(carrinho_id, items);
        }
    }

    public void cartRemove(int product_id){
        items.remove(product_id);
        cartsAll.remove(carrinho_id);
    }


}
